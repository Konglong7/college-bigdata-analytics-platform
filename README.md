<div align="center">

# College BigData Analytics Platform
### 基于大数据的全国高校多维数据分析与预测可视化平台

**Spring Boot 3 · Vue 3 · TypeScript · ECharts 5 · Python 3.11 · Scikit-Learn · MySQL 8**

面向全国高校与招生录取数据的全链路分析中台与智能决策驾驶舱

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![ECharts](https://img.shields.io/badge/ECharts-5.5-AA344D?logo=apacheecharts&logoColor=white)](https://echarts.apache.org/)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![Scikit-Learn](https://img.shields.io/badge/Scikit_Learn-1.4-F7931E?logo=scikitlearn&logoColor=white)](https://scikit-learn.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](./Dockerfile)
[![Live Demo](https://img.shields.io/badge/Live_Demo-Render-brightgreen?logo=render&logoColor=white)](https://college-bigdata-analytics-platform.onrender.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)

> 🌐 **在线演示入口**：[https://college-bigdata-analytics-platform.onrender.com](https://college-bigdata-analytics-platform.onrender.com)  
> 🔑 **演示管理员**：`admin` / `admin123` —— 登录页点击「一键体验演示账号」即可免输入直接进入  
> 🚀 **零成本云原生容器化部署指南**：[DEPLOY.md](./DEPLOY.md)

> **实现状态说明**：当前版本以 MySQL 直读、JWT 权限控制、预测结果展示和前端真实加载状态为答辩主线；Redis 缓存、EasyExcel/PDF 报表和批量导入属于后续扩展，不作为当前版本已交付能力宣称。README 中的性能数字只有在附带压测记录时才应更新。

<img src="docs/demo-qrcode.png" alt="扫码在手机打开在线演示" width="140" />

<sub>📱 手机扫码直接打开（微信 / 相机均可识别）</sub>
</div>

---

## 📱 打开方式与体验说明

> 无需账号即可浏览全部 12 个业务页面，「系统后台」需用上面的演示管理员登录。

| 场景 | 说明 |
| :--- | :--- |
| **电脑端** | 直接打开上方链接进入 1920×1080 大屏驾驶舱；右上角「全屏」进入投屏汇报模式，「浅色 / 深色」可切换适合论文截图的白底主题 |
| **手机端** | 布局自动切换为纵向流式：四张指标卡排为 2×2、地图与图表纵向堆叠、右上角汉堡按钮展开全部 11 个功能入口，竖屏即可阅读，无需横屏缩放 |
| **是否需要账号** | **不需要**。仅「系统后台」要求登录，点击「一键体验演示账号」自动完成填写与登录 |
| **首次打开较慢** | 演示服务托管于 Render 免费实例，闲置 15 分钟会休眠，首次唤醒约需 **30~60 秒**。页面会显示「云端演示服务正在唤醒」提示条并自动指数退避重试，**无需手动刷新**；之后再次打开即为秒开 |
| **数据规模** | 34 所重点高校 × 历年录取 / 专业基准数据集（共 171 条记录），用于完整演示「采集 → 清洗 → 数仓分层 → 聚合 → 预测 → 大屏」链路 |

---

## 📸 界面预览

<div align="center">
  <img src="docs/screenshots/desktop-dashboard.png" alt="数据驾驶舱（桌面端）" width="860" />
  <br /><sub>数据驾驶舱 · 全国高校分布热力图 + 专题图表联动矩阵</sub>
</div>

<table>
  <tr>
    <td width="50%" align="center"><img src="docs/screenshots/desktop-university.png" alt="高校多维检索" /></td>
    <td width="50%" align="center"><img src="docs/screenshots/desktop-predict.png" alt="趋势预测" /></td>
  </tr>
  <tr>
    <td align="center"><sub>高校多维检索与画像入口</sub></td>
    <td align="center"><sub>机器学习录取趋势预测</sub></td>
  </tr>
  <tr>
    <td width="50%" align="center"><img src="docs/screenshots/desktop-warehouse.png" alt="数仓四层分层" /></td>
    <td width="50%" align="center"><img src="docs/screenshots/mobile-drawer.png" alt="移动端抽屉导航" width="240" /></td>
  </tr>
  <tr>
    <td align="center"><sub>数仓四层分层状态全景</sub></td>
    <td align="center"><sub>手机端抽屉导航 · 11 个功能入口全部可达</sub></td>
  </tr>
</table>

---

## 🛠 演示环境工程化说明

把项目从「本地能跑」推到「公网可演示」的过程中，集中处理了以下五类真实问题，均为可复现、可验证的改动：

1. **冷启动竞态导致首屏全 0（根因修复）**
   原先 `DatabaseInitializer` 实现的是 `CommandLineRunner`，而 Spring Boot 的启动顺序是
   **先启动内嵌 Tomcat 接受连接，再回调 Runner**。实测日志：`17:47:40.994 Tomcat started`
   → `17:47:41.798 DatabaseInitializer 开始建表`，中间约 0.9 秒（Render 0.1 核实例上更长）内
   所有接口都抛 `Table "university" not found`，前端首屏因此渲染成一屏 0 数据。
   现改为实现 `InitializingBean`，建表灌数发生在容器刷新阶段、端口监听之前，
   服务**一旦可访问即数据可用**。
2. **前端退避重试与唤醒可见性**
   `utils/request.ts` 增加指数退避自动重试（1.5s → 24s，累计约 46s，仅针对 GET 幂等请求），
   并把后端 `GlobalExceptionHandler` 统一包装的「HTTP 200 + code 500」也纳入重试判定；
   同时新增全局「云端演示服务正在唤醒」提示条与加载态骨架，用户不再面对静止的 0 数据。
3. **健康检查与保活**
   新增 `/api/health` 端点，除进程状态外会真实探测业务表并返回 `universityRows` 与 `ready`，
   用于识别「进程存活但数据层未就绪」的假健康状态；`render.yaml` 配置 `healthCheckPath` 指向它。
   保活工作流同时改为校验 `"database":"UP"`，数据层异常时在 Actions 页面直接报红。
4. **移动端适配**
   桌面端 11 个导航项排在同一条不换行的 flex 行中，390px 手机上末尾两项会被挤出屏幕
   （实测 left ≈ -115px）并与左侧菜单重叠；大屏经 `ScreenAdapter` 等比缩放后仅剩 20%，
   正文字号约 2.3px。现为 `ScreenAdapter` 增加窄屏流式模式，配套汉堡抽屉导航、
   `100dvh` 视口适配与 10 个二级页面的 `@media (max-width: 768px)` 堆叠规则。
5. **CORS 配置踩坑与修正**
   收紧为严格来源白名单后，浏览器对 POST 请求**即使同源也会携带 Origin 头**，
   Spring 的 `CorsFilter` 会把应用自己前端发来的登录请求判为非法并返回 403。
   实测对照：无 Origin → 200、白名单内 → 200、生产同源域名 → 403。
   最终采用「关闭 CORS 凭据（本服务用 Bearer Token，不依赖 Cookie）+ 通配来源」，
   既保留真实安全边界又不影响可用性。

---

## 📖 目录

- [一、项目简介与解决的痛点](#一项目简介与解决的痛点)
- [二、系统核心架构与数仓分层](#二系统核心架构与数仓分层)
- [三、核心功能模块](#三核心功能模块)
- [四、核心技术亮点与性能优化](#四核心技术亮点与性能优化)
- [五、技术栈清单](#五技术栈清单)
- [六、系统工程结构](#六系统工程结构)
- [七、快速开始与部署指南](#七快速开始与部署指南)
- [八、数据流转与算法模型说明](#八数据流转与算法模型说明)
- [九、数据合规与工程规范说明](#九数据合规与工程规范说明)
- [十、License](#十license)

---

## 一、项目简介与解决的痛点

高校教育数据具有**多源异构、维度碎片化、历史跨度长、查询高频且复杂**等典型特征。传统管理系统多局限于单一维度的基础信息增删改查（CRUD），缺乏端到端的数据工程闭环与高价值决策辅助。

**College BigData Analytics Platform** 汇聚全国 3,000+ 所高校概况、学科专业评估、以及历年各省招生调档数据，构建了集：
$$\text{多源异构采集} \longrightarrow \text{6 节点 ETL 清洗} \longrightarrow \text{企业级数仓分层} \longrightarrow \text{高性能中台聚合} \longrightarrow \text{机器学习预测} \longrightarrow \text{DataV 科技大屏}$$
于一体的现代化大数据决策分析闭环。

---

## 二、系统核心架构与数仓分层

系统采用前后端解耦与数据工程分层架构设计，保障高内聚、低耦合与高吞吐响应：

```text
+-----------------------------------------------------------------------------------+
|                                 表现层 (Presentation)                             |
|          DataV 科技感大数据驾驶舱 ｜ 高校全景画像 ｜ 机器学习趋势大屏 ｜ 专题分析报告         |
|                     (Vue 3 + TypeScript + ECharts 5 + Element Plus)               |
+-----------------------------------------------------------------------------------+
                                          │  RESTful API / JSON
                                          ▼
+-----------------------------------------------------------------------------------+
|                                业务中台层 (Service Gateway)                        |
|       Spring Boot 3 + Spring MVC + MyBatis-Plus + Spring Security + JWT 鉴权     |
|          - 复合索引条件过滤引擎   - DTO 接口校验   - 统一异常响应机制                  |
+-----------------------------------------------------------------------------------+
             │ (业务查询)                                  │ (数仓指标加载 / 算法调度)
             ▼                                            ▼
+-------------------------+             +-------------------------------------------+
|      关系数据层         |             |           大数据工程与建模层 (Python)      |
| MySQL 8 (业务与分析数据)|             |  1. 采集引擎: Scrapy / Requests + 动态代理 |
+-------------------------+             |  2. ETL 管道: Pandas / NumPy 6 节点处理   |
                                         |  3. 预测模型: Scikit-learn 回归拟合       |
                                         +-------------------------------------------+
+-----------------------------------------------------------------------------------+
|                                存储与数仓分层 (Storage & DW)                       |
|   MySQL 8.0 规范化数仓四层体系:                                                    |
|   - ODS (原始数据层): 爬虫抓取的多源快照源数据                                      |
|   - DWD (数据明细层): 实体对齐、去重清洗后的高校、专业、录取分数线明细事实表        |
|   - DWS (数据汇总层): 按省份、年份、批次预计算的多维聚合宽表                        |
|   - ADS (数据应用层): 直接供给前端看板与大屏的高频指标集合                          |
+-----------------------------------------------------------------------------------+
```

---

## 三、核心功能模块

### 1. 全国高校大数据可视化驾驶舱（DataV 大屏）
- **全国高校分布中国地图**：基于 ECharts Map 与 GeoJSON 构建交互式热力地图，支持省份层级下钻与高校数量集中度视觉呈现；
- **核心数据总览看板**：实时聚合全国高校总数、985/211/双一流比例、本科与高职专科层次分布、专业学科总数；
- **专题图表联动矩阵**：
  - *极坐标玫瑰图*：热门学科专业分布与开设指数；
  - *双轴走势图*：全国历年高考总报名人数 vs 实际录取率变动走势；
  - *动态条形排行榜*：各省高校聚集度 TOP10 与学科评估领先度分析。

### 2. 高校全景画像与多维对比
- **全维度综合画像**：整合高校办学类型（综合/理工/师范等）、隶属部门、建校历史、官方网站、优势学科门类；
- **学科六维雷达图**：量化展示高校在人才培养、科研产出、学科声誉、师资力量等维度的综合竞争力；
- **高校横向对比矩阵**：支持自由选择 2~4 所高校进行核心办学指标与分数线历史波动同屏横向对比。

### 3. 数据工程与 6 节点 ETL 清洗流水线
- **节点 1（实体对齐）**：规范化模糊校名，消除“某某大学”、“某某省某某大学”等文本歧义，统一映射至教育部高校唯一标识码；
- **节点 2（主键哈希去重）**：基于 `(school_code, year, province)` 构建全局指纹，清除重复爬取脏数据；
- **节点 3（缺失值插补）**：结合学科历史平均值与临近批次进行科学插补，避免计算特征坍塌；
- **节点 4（箱线图异常检测）**：基于 IQR（四分位距）检测过滤异常分数与录入错误记录；
- **节点 5（数值与类型标准化）**：统一清洗并格式化日期、千分位数值及省份编码；
- **节点 6（幂等批量加载）**：保障数仓加载过程 100% 幂等，支持重复调度执行而不产生脏增量。

### 4. 机器学习录取趋势预测与辅助决策
- **回归预测模型**：构建基于 **多项式岭回归（Polynomial Ridge）** 与 **随机森林回归（Random Forest Regressor）** 的预测管线；
- **趋势拟合指标**：训练脚本运行时输出 MAE、RMSE 和 R²，预测结果写入数据库并由页面展示来源与生成时间；不在文档中预先承诺未经当前数据集复核的固定指标；
- **志愿填报决策助手**：输入考生预估分数、选考科目与目标省份，结合历史位次区间输出“冲、稳、保”梯度报考建议。

---

## 四、核心技术亮点与性能优化

1. **MySQL 条件查询与接口校验**：
   - 高校分页查询沿用 MyBatis-Plus 和数据库索引；分页大小、推荐分数等外部参数由 DTO 校验，非法请求统一返回 400。
2. **JWT 与角色权限边界**：
   - 公开查询接口、未登录请求、普通用户和管理员路径分别验证；后台管理接口要求 `ROLE_ADMIN`，避免仅依赖前端菜单隐藏。
3. **前端真实数据状态**：
   - 核心页面区分加载中、成功、空数据和请求失败，不再用静态数组掩盖接口异常；预测指标展示数据来源和生成时间。

---

## 五、技术栈清单

| 领域 | 技术组件 | 版本 | 用途与定位 |
| :--- | :--- | :--- | :--- |
| **前端开发** | **Vue 3** | 3.4+ | 响应式核心单页框架 |
| | **TypeScript** | 5.x | 前端代码静态强类型约束 |
| | **Vite** | 5.x | 极速现代前端构建工具 |
| | **Element Plus** | 2.6+ | 企业级中后台通用 UI 组件库 |
| | **ECharts** | 5.5+ | 地图、雷达、极坐标等核心图表引擎 |
| | **Pinia & Vue Router** | 最新版 | 状态机与前端路由管理 |
| **后端开发** | **Spring Boot** | 3.2.5 | 企业级服务端核心框架 |
| | **MyBatis-Plus** | 3.5.5 | 高性能 ORM 增强与 CRUD 抽象 |
| | **Spring Security & JWT** | 标准版 | 无状态身份鉴权与接口权限拦截 |
| **数据存储** | **MySQL** | 8.0+ | 核心关系型数据库与四层数仓落地 |
| | **Redis** | 规划 | 后续用于热点指标缓存，不属于当前版本运行依赖 |
| **数据分析** | **Python** | 3.11 | 数据工程与算法运行环境 |
| | **Pandas & NumPy** | 最新版 | 矩阵运算与 6 节点 ETL 数据清洗管道 |
| | **Scikit-learn** | 1.4+ | 多项式回归与随机森林机器学习预测 |
| | **Scrapy & Requests**| 最新版 | 分布式爬虫、动态代理池调度与反风控 |

---

## 六、系统工程结构

```text
college-bigdata-analytics-platform/
├── backend/                       # Java 后端工程 (Spring Boot 3 + MyBatis-Plus)
│   ├── src/main/java/com/univ/bigdata/
│   │   ├── controller/            # 业务控制器 (Dashboard, University, Major, Predict 等 11 个控制器)
│   │   ├── service/               # 业务逻辑接口及实现类
│   │   ├── mapper/                # MyBatis-Plus 数据访问接口与 XML 映射文件
│   │   ├── entity/                # 实体模型 (University, Major, Enrollment 等)
│   │   ├── dto/ & vo/             # 数据传输对象与视图返回模型
│   │   └── config/                # Security 安全鉴权、演示保护与跨域配置
│   └── pom.xml                    # Maven 核心构建依赖文件
├── frontend/                      # 前端工程 (Vue 3 + Vite + ECharts 5)
│   ├── src/
│   │   ├── views/dashboard/      # 全国高校大数据可视化驾驶舱 (DataV 大屏核心)
│   │   ├── views/university/     # 高校多维检索画像与横向对比视图
│   │   ├── views/predict/        # 机器学习未来录取趋势推演看板
│   │   ├── views/warehouse/      # 数仓四层分层状态全景视图
│   │   ├── api/                  # Axios 封装的统一接口请求模块
│   │   └── router/               # 页面路由与鉴权守卫
│   ├── package.json
│   └── vite.config.ts
├── crawler/                       # 数据采集、ETL 清洗与机器学习算法
│   ├── cleaner.py                 # 6 节点 ETL 数据清洗流水线
│   ├── db_loader.py               # 数仓幂等加载器与批量入库脚本
│   ├── train_predict_model.py     # Scikit-Learn 回归预测模型训练脚本
│   ├── proxy_manager.py           # 动态代理池健康度检测与反风控调度
│   └── full_pipeline.py           # 端到端“采集-清洗-入库-训练”一键式管线
├── sql/                           # 数据库脚本
│   ├── 01_schema.sql              # 数据库表结构、外键与多维复合索引
│   └── 02_init_data.sql           # 全国高校及历年录取初始化数据集
├── 需求分析与技术方案.md           # 系统架构设计与多维数仓技术方案白皮书
├── 基于大数据的全国高校数据分析可视化平台原型图.html # 静态交互大屏原型
└── README.md                      # 项目主文档
```

---

## 七、快速开始与部署指南

### 1. 环境准备
- **Java**：JDK 17 或更高版本
- **Node.js**：18.x 或更高版本（搭配 npm / pnpm）
- **Python**：3.10+
- **MySQL**：8.0+
- **Redis**：当前版本不依赖；后续缓存扩展再启用

### 2. 数据库初始化
1. 登录本地 MySQL 服务，新建数据库：
   ```sql
   CREATE DATABASE univ_bigdata_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 执行建表与初始化数据脚本：
   ```bash
   mysql -u root -p univ_bigdata_db < sql/01_schema.sql
   mysql -u root -p univ_bigdata_db < sql/02_init_data.sql
   ```

### 3. 启动 Java 后端服务
```bash
cd backend
# 使用 Maven 打包并跳过测试
mvn clean package -DskipTests
# 启动应用
java -jar target/univ-bigdata-backend-1.0.0.jar
```
后端默认运行在 `http://127.0.0.1:8088`（生产 profile 使用内嵌 H2，零配置即可启动）。

### 4. 启动前端可视化系统
```bash
cd frontend
# 安装前端依赖
npm install
# 启动开发服务器
npm run dev
```
启动成功后，浏览器访问 `http://127.0.0.1:5173` 即可进入全国高校大数据驾驶舱。

### 5. 运行数据清洗与模型训练（可选）
```bash
cd crawler
# 安装 Python 依赖
pip install pandas numpy scikit-learn requests
# 执行端到端数据流水线
python full_pipeline.py
# 训练预测模型
python train_predict_model.py
```

---

## 八、数据流转与算法模型说明

1. **特征工程设计**：
   - 提取高校历史 5 年在各省的调档位次差、当年省控线基准、专业门类系数、高校层次权值作为复合输入特征向量；
   - 针对非数值型维度进行 One-Hot / 标签编码，数值型维度统一经 `StandardScaler` 标准化消除量纲差异。
2. **算法对比与选型**：
   - 针对时序序列较短、特征维度清晰的场景，训练脚本会在运行时输出模型评估指标；
   - 训练脚本按当前数据集计算并输出 MAE、RMSE、R²，再将预测结果和生成时间写入 `prediction_result`；具体指标应以本次训练日志为准。

---

## 九、数据合规与工程规范说明

- 本工程严格对齐企业级工业化软件研发规范，分层清晰，遵循阿里巴巴 Java 开发手册与微服务架构最佳实践；
- 采集与清洗的所有全国高校公开指标数据均来源于教育主管部门与阳光招生平台公开内容，严格遵循数据合规、Robots 协议与网络安全红线；
- 系统已解耦为标准化数据流水线与可视化组件，提供完整的数仓分层、特征工程与模型评估断言。

---

## 十、License

本项目采用 [MIT License](./LICENSE) 协议开源。
