# 全国高校数据分析可视化平台 全栈云原生零成本部署实战手册

> **适用场景**：将 Spring Boot 3 + Vue 3 + ECharts 5 全国高校数据分析与智能推荐可视化平台，以 **100% 永久零成本**、**高可用**、**免备案独立域名** 方式上线部署，供简历项目展示与面试官/访客实时在线体验。

---

## 目录
1. [架构全景与零成本选型](#一架构全景与零成本选型)
2. [代码与容器化配置（已在代码库固化）](#二代码与容器化配置)
3. [云端数据库准备（约 1 分钟）](#三云端数据库准备)
4. [推送代码至 GitHub 仓库](#四推送代码至-github-仓库)
5. [Render Web Service 容器化部署（约 3 分钟）](#五render-web-service-容器化部署)
6. [7×24 小时常驻秒开保活体系（防休眠）](#六724-小时常驻秒开保活体系防休眠)
7. [简历呈现与面试答辩亮点](#七简历呈现与面试答辩亮点)

---

## 一、架构全景与零成本选型

```
                      ┌───────────────────────────────────────────┐
                      │    双重保活体系 (防 15min 休眠)             │
                      │  • GitHub Actions (每 12 分钟心跳)          │
                      │  • Cron-Job.org   (每 10 分钟心跳)          │
                      └─────────────────────┬─────────────────────┘
                                            │ HTTP GET
                                            ▼
┌──────────────────┐               ┌─────────────────────────────────┐
│  访客 / 面试官    ├──────────────►│   Render Cloud (Free Tier)      │
│  Browser / HTTPS │ 专属免费域名    │   • 0.1 CPU / 512 MB RAM        │
│                  │               │   • Alpine JRE 17 容器          │
│                  │               │   • Vue 3 静态托管 + SpringBoot   │
└──────────────────┘               └────────────────┬────────────────┘
                                                    │
                                         JDBC / TLS │ (MySQL 8.0 协议)
                                                    ▼
                                           ┌────────────────┐
                                           │  TiDB Cloud    │
                                           │  Serverless    │
                                           │  (免费 5GB 存储)│
                                           └────────────────┘
```

| 组件 | 选用服务 | 免费额度 / 成本 | 核心优势 |
| :--- | :--- | :--- | :--- |
| **代码托管** | **GitHub** | 永久免费 | 全球通用、集成 Actions CI/CD 流水线 |
| **容器计算** | **Render Web Service** | 免费层（512MB RAM） | 支持多阶段 Dockerfile、自带泛域名 HTTPS 证书 |
| **关系型数据库** | **TiDB Cloud Serverless** | 免费层（5GB 存储） | 100% 兼容 MySQL 8.0 协议、亚太节点低延迟、免运维 |
| **心跳保活** | **GitHub Actions + Cron-Job.org** | 永久免费 | 双重守护，解决 Render 免费容器 15 分钟休眠问题 |

---

## 二、代码与容器化配置

本项目已在代码仓库中完成了生产容器化配置的全面固化：

### 1. 多阶段构建 Dockerfile
根目录 [`Dockerfile`](./Dockerfile) 采用三阶段流水线：
- **Stage 1 (前端)**：`node:20-alpine` 自动执行 `npm install` 与 `npm run build`，编译产物自动注入 Spring Boot 静态资源目录并完成 Gzip 预压缩。
- **Stage 2 (后端)**：`maven:3.9-eclipse-temurin-17-alpine` 打包 Spring Boot 生产 Fat Jar。
- **Stage 3 (运行)**：`eclipse-temurin:17-jre-alpine` 超轻量底座。
- **关键 JVM 调优参数**（严格约束在 300MB 以内，防止 Render 512MB 容器 OOM）：
  ```dockerfile
  ENV JAVA_OPTS="-Xms128m -Xmx300m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k -Djava.security.egd=file:/dev/./urandom"
  ```
- **自适应端口绑定**：
  ```dockerfile
  ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
  ```

### 2. 生产配置文件 `application-prod.yml`
位于 [`backend/src/main/resources/application-prod.yml`](./backend/src/main/resources/application-prod.yml)：
- 端口监听：`${PORT:8088}`（自适应 Render 注入的动态端口，通常为 10000）。
- 数据库连接池：采用 HikariCP，将连接数精简至 `minimum-idle: 2`, `maximum-pool-size: 5`，大幅降低空闲内存占用。
- Gzip 传输压缩：响应体大于 1KB 时自动开启 Gzip，降低网络首屏时延。
- 演示模式拦截：开启 `univ.demo-mode: true`，全局拦截破坏性写操作，保障基准演示数据完整。

---

## 三、云端数据库准备

### TiDB Cloud Serverless MySQL (约 1 分钟)
1. 访问 [TiDB Cloud 控制台](https://tidbcloud.com/) 并通过 GitHub 一键登录。
2. 点击 **Create Cluster**：
   - Cluster Type：选择 **Serverless**（$0 Free）。
   - Region：推荐选择 **AWS / Tokyo (ap-northeast-1)** 或 **Singapore**。
   - Cluster Name：填写 `univ-bigdata-db`。
   - 点击 **Create**。
3. 进入集群面板，点击 **Connect**：
   - 记录 **Host**、**Port (4000)**、**User** 和 **Password**。
4. 导入数据表与初始化基准数据：
   - 在控制台左侧菜单点击 **Chat2Query** 或 **SQL Editor**；
   - 先执行建库：
     ```sql
     CREATE DATABASE IF NOT EXISTS `univ_bigdata_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     USE `univ_bigdata_db`;
     ```
   - 依次执行项目中的两个 SQL 脚本内容：
     1. [`sql/01_schema.sql`](./sql/01_schema.sql)（用户、高校、专业、录取、预测及日志 7 张核心表结构与索引）
     2. [`sql/02_init_data.sql`](./sql/02_init_data.sql)（30 所全国重点高校、各维度招生与分数基准数据）

---

## 四、推送代码至 GitHub 仓库

```powershell
# 1. 切换到项目根目录
cd "D:\Desktop\code\基于大数据的全国高校数据分析可视化平台"

# 2. 检查并提交代码
git add .
git commit -m "feat: complete cloud-native dockerization, spa routing and keepalive configuration"

# 3. 推送至远程仓库
git push -u origin main
```

---

## 五、Render Web Service 容器化部署

1. 打开 [Render Dashboard](https://dashboard.render.com/)（GitHub 一键授权登录）。
2. 点击右上角 **New +** -> **Web Service**。
3. 选择 **Build and deploy from a Git repository**，选中仓库 `college-bigdata-analytics-platform`。
4. 填写基本配置：
   - **Name**：`college-bigdata-analytics-platform`（分配的免费二级域名为 `https://college-bigdata-analytics-platform.onrender.com`）
   - **Region**：选择与数据库最近的区域（如 `Singapore` 或 `Oregon`）
   - **Branch**：`main`
   - **Language / Runtime**：**Docker**（Render 会自动识别并运行根目录下的 Dockerfile）
   - **Instance Type**：**勾选 $0 / month Free**（512 MB RAM, 0.1 CPU）
5. **配置环境变量（Environment Variables）**：
   点击 **Add from .env** 按钮，粘贴以下内容（将对应参数替换为你真实的 TiDB 凭据）：

   ```env
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=jdbc:mysql://<你的TiDB主机>:4000/univ_bigdata_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=true
   SPRING_DATASOURCE_USERNAME=<你的TiDB用户名>
   SPRING_DATASOURCE_PASSWORD=<你的TiDB密码>
   ```

6. 点击 **Deploy web service**：
   - 容器编译约 2~3 分钟，当日志显示 `Tomcat started on port 10000` 且标记为 `Live` 后，即可直接访问！

---

## 六、7×24 小时常驻秒开保活体系（防休眠）

> **Render 免费容器机制**：15 分钟无请求会自动进入挂起状态，下次访问需要冷启动（约 40 秒）。通过自动化定时心跳，可实现 7×24 小时常驻秒开。

### 1. 第一重：GitHub Actions 云端定时心跳（代码库已内置）
已内置 [`.github/workflows/keepalive.yml`](./.github/workflows/keepalive.yml)，每 12 分钟由 GitHub 云端自动发送轻量 HEAD/GET 请求。

### 2. 第二重：Cron-Job.org 外部高精度心跳（双重保险）
1. 访问 [Cron-Job.org 控制台](https://console.cron-job.org/jobs) 并免费注册登录。
2. 点击 **CREATE CRONJOB**：
   - **Title**：`College-Bigdata-KeepAlive`
   - **URL**：`https://college-bigdata-analytics-platform.onrender.com`
   - **Schedule**：Every 10 minutes (`*/10 * * * *`)
   - **Enable job**：勾选并保存。

---

## 七、简历呈现与面试答辩亮点

### 1. 简历项目描述推荐模板

```markdown
### 全国高校大数据分析与可视化平台 (全栈架构 & 云原生容器化部署)
- **在线演示**：https://college-bigdata-analytics-platform.onrender.com
- **源码仓库**：https://github.com/Konglong7/college-bigdata-analytics-platform
- **技术栈**：Vue 3 + Vite + ECharts 5 + Element Plus + Spring Boot 3 + MyBatis-Plus + TiDB Cloud + Docker
- **工程亮点**：
  1. 容器化交付：设计多阶段 Multi-stage Docker 流水线，实现前端 Vite 资源自动内嵌、Gzip 预压缩与 Spring Boot 胖 Jar 统一交付；
  2. 极低资源调优：针对云原生低配容器（512MB RAM）进行精准 JVM 堆内存与 GC 约束（SerialGC、Xmx300m），彻底消除小内存环境下的 OOM 隐患；
  3. SPA 路由与资源强缓存：通过 Spring MVC 资源链与 PathResourceResolver 实现全路由 History 模式 fallback，静态资产配置 1 年版本化强缓存；
  4. 生产安全与高可用：内置 Demo 模式切面拦截器防止基准数据被恶意修改，配合 GitHub Actions + Cron-Job 双心跳机制实现 7×24 小时常驻秒开。
```

### 2. 面试高频问题与答辩思路

**Q1：为什么前端不需要单独用 Nginx 反代，而是整合进 Spring Boot 静态托管？**
> **答辩思路**：
> 1. 在云原生低配/免租金容器场景（如 Render Free Tier 单容器 512MB 限制）下，若同时运行 Nginx + Java 两个守护进程，既增加容器内存开销，又增加跨进程网络转发损耗；
> 2. Spring Boot 内部内嵌 Tomcat/Undertow，配合自定义 `ResourceHandlerRegistry` 和 `EncodedResourceResolver`，可以直接提供不输 Nginx 的静态资源强缓存、304 协商缓存及 Gzip 预压缩直发；
> 3. 基于 `PathResourceResolver` 实现 SPA 路由回退，使单个 Docker 镜像具备完整的全栈独立部署与一键迁移能力。
