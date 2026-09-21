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
ENV JAVA_OPTS="-Xms128m -Xmx300m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8088

EXPOSE 8088

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
