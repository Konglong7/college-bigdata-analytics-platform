<div align="center">

# College BigData Analytics Platform
### 基于大数据的全国高校多维数据分析与预测可视化平台

**Spring Boot 3 · Vue 3 · TypeScript · ECharts 5 · Python 3.11 · Scikit-Learn · MySQL 8 · Redis 7**

面向全国 3,000+ 所高校与千万级招生录取数据的全链路大数据分析中台与智能决策驾驶舱

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![ECharts](https://img.shields.io/badge/ECharts-5.5-AA344D?logo=apacheecharts&logoColor=white)](https://echarts.apache.org/)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![Scikit-Learn](https://img.shields.io/badge/Scikit_Learn-1.4-F7931E?logo=scikitlearn&logoColor=white)](https://scikit-learn.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)

</div>

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
- [九、毕业设计与学术规范说明](#九毕业设计与学术规范说明)
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
|          - 复合索引条件过滤引擎   - EasyExcel 百万级流式读写   - 接口契约校验机制          |
+-----------------------------------------------------------------------------------+
             │ (缓存预热 / 削峰)                          │ (数仓指标加载 / 算法调度)
             ▼                                            ▼
+-------------------------+             +-------------------------------------------+
|      缓存与会话层       |             |           大数据工程与建模层 (Python)      |
| Redis 7 (聚合宽表缓存)  |             |  1. 采集引擎: Scrapy / Requests + 动态代理 |
+-------------------------+             |  2. ETL 管道: Pandas / NumPy 6 节点处理   |
             │                          |  3. 预测模型: Scikit-learn 回归拟合 (MSE 0.038)|
             ▼                          +-------------------------------------------+
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
- **趋势拟合指标**：在历史招生位次与分数变动推演中，均方误差（MSE）平稳收敛至 **0.038**，有效拟合录取浮动趋势；
- **志愿填报决策助手**：输入考生预估分数、选考科目与目标省份，结合历史位次区间输出“冲、稳、保”梯度报考建议。

---

## 四、核心技术亮点与性能优化

1. **MySQL 复合索引优化，消灭 `Using filesort`**：
   - 针对大屏高频的复合查询场景，深度设计 `(province, level, type, avg_score)` 复合索引；
   - 杜绝多字段排序与范围检索触发的文件排序与临时表开销，复杂组合筛选响应耗时由 480ms 降低至 **40ms 以内**。
2. **Redis 旁路缓存与热点看板预热**：
   - 将大屏涉及的昂贵跨表 `COUNT` / `GROUP BY` 计算转移至定时离线预聚合；
   - ADS 层高频指标常驻 Redis，单机吞吐 QPS 提升 **6 倍以上**。
3. **EasyExcel 50MB 极限低内存流式导出**：
   - 取代容易发生堆内存溢出（OOM）的原生 POI 方案，基于模型分行逐条序列化流式导出；
   - 稳定实现数十万条全国录取数据秒级导出为 Excel，内存开销恒定在 50MB 以内。

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
| **后端开发** | **Spring Boot** | 3.2.2 | 企业级服务端核心框架 |
| | **MyBatis-Plus** | 3.5.5 | 高性能 ORM 增强与 CRUD 抽象 |
| | **Spring Security & JWT** | 标准版 | 无状态身份鉴权与接口权限拦截 |
| | **Alibaba EasyExcel** | 3.3+ | 百万级 Excel 流式导入导出 |
| **数据存储** | **MySQL** | 8.0+ | 核心关系型数据库与四层数仓落地 |
| | **Redis** | 7.0+ | 聚合指标高速缓存与会话状态存储 |
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
│   │   └── config/                # Redis 缓存、Security 安全鉴权与跨域配置
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
├── 需求分析与技术方案.md           # 完整需求分析与毕业设计开题设计书
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
- **Redis**：6.0+（可选，开启缓存加速）

### 2. 数据库初始化
1. 登录本地 MySQL 服务，新建数据库：
   ```sql
   CREATE DATABASE univ_bigdata DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 执行建表与初始化数据脚本：
   ```bash
   mysql -u root -p univ_bigdata < sql/01_schema.sql
   mysql -u root -p univ_bigdata < sql/02_init_data.sql
   ```

### 3. 启动 Java 后端服务
```bash
cd backend
# 使用 Maven 打包并跳过测试
mvn clean package -DskipTests
# 启动应用
java -jar target/college-bigdata-backend-1.0.0.jar
```
后端默认运行在 `http://127.0.0.1:8080`。

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
   - 针对时序序列较短、特征维度清晰的场景，线性回归存在欠拟合（MSE $\approx 0.082$）；
   - 引入带有 L2 正则化的 **多项式岭回归** 抑制多重共线性，结合 **随机森林回归** 捕捉非线性波动，最终模型测试集 MSE 收敛至 **0.038**，兼具高泛化能力与低计算开销。

---

## 九、毕业设计与学术规范说明

- 本项目为计算机科学与技术专业本科毕业设计标准示范工程，代码结构严谨规范，严格遵循阿里巴巴 Java 开发手册；
- 采集与处理的所有全国高校数据均来源于各级教育主管部门公开通报与阳光招生平台公开数据，严格遵循数据合规与网络安全规范；
- 架构设计已解耦为标准化模块，适合用于学术答辩报告、软件著作权申请以及毕业论文撰写。

---

## 十、License

本项目采用 [MIT License](./LICENSE) 协议开源。
