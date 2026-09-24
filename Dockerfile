# Stage 1: Build Frontend (Vue 3 + Vite)
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# Stage 2: Build Backend (Maven + Eclipse Temurin 17)
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-builder
WORKDIR /app

COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src

# 拷贝前端构建产物至 Spring Boot 静态资源目录并执行 Gzip 预压缩
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static
RUN find ./backend/src/main/resources/static -type f \( -name "*.js" -o -name "*.css" \) -exec gzip -k -9 {} + && \
    cd backend && mvn clean package -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 拷贝 Fat Jar
COPY --from=backend-builder /app/backend/target/univ-bigdata-backend-1.0.0.jar app.jar

# JVM options optimized for Render 512MB RAM free tier
#
# 参数说明（Render 免费实例：0.1 核 CPU / 512MB 内存）：
#   -Xmx288m -XX:MaxMetaspaceSize=112m  为 JVM native/CodeCache/线程栈预留约 110MB，避免贴顶被 OOMKill(137)
#   -XX:TieredStopAtLevel=1             只启用 C1 编译器，显著缩短冷启动时间并降低 CodeCache 占用
#                                         （代价是峰值吞吐下降，对演示场景是划算的取舍）
#   -XX:+ExitOnOutOfMemoryError         内存溢出时让容器干净退出并重启，而不是进入半死不活的假活状态
ENV JAVA_OPTS="-Xms96m -Xmx288m -XX:MaxMetaspaceSize=112m -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8088

EXPOSE 8088

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
