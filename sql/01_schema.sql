-- =========================================================
-- 《基于大数据的全国高校数据分析可视化平台》
-- 数据库初始化 DDL 脚本 (MySQL 8.0+)
-- =========================================================

CREATE DATABASE IF NOT EXISTS `univ_bigdata_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `univ_bigdata_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------
-- 1. 系统用户及权限表 (sys_user)
-- 作用: 支撑用户注册登录、JWT 鉴权及基于角色的访问控制 (RBAC)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一主键',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号 (唯一)',
  `password` VARCHAR(128) NOT NULL COMMENT '加密存储密码 (BCrypt 格式)',
  `role` VARCHAR(32) NOT NULL DEFAULT 'ROLE_USER' COMMENT '用户角色: ROLE_ADMIN(管理员) / ROLE_USER(普通用户)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户与角色权限表';

-- ---------------------------------------------------------
-- 2. 全国高校基础信息表 (university)
-- 作用: 核心实体表，存储全国高校名录、地区、办学类型、层次及建校年份
-- 映射页面: 数据驾驶舱 (总数/地图/省份排行/类型占比)、高校查询列表、高校详情画像、系统后台管理
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `university`;
CREATE TABLE `university` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '高校唯一主键',
  `school_name` VARCHAR(128) NOT NULL COMMENT '高校名称',
  `school_code` VARCHAR(32) DEFAULT NULL COMMENT '教育部全国高校唯一标识码',
  `province` VARCHAR(32) NOT NULL COMMENT '所在省份/直辖市 (如: 北京, 江苏, 广东)',
  `city` VARCHAR(32) NOT NULL COMMENT '所在城市 (如: 北京, 南京, 广州)',
  `school_type` VARCHAR(32) NOT NULL COMMENT '学校类型: 综合类 / 理工类 / 师范类 / 财经类 / 医药类 / 其他',
  `school_level` VARCHAR(32) NOT NULL COMMENT '办学层次: 985/211 / 211工程 / 普通本科 / 专科(高职)',
  `establish_year` INT DEFAULT NULL COMMENT '建校年份 (如: 1898, 1911)',
  `introduction` TEXT DEFAULT NULL COMMENT '高校发展简史与办学特色介绍',
  `raw_school_id` INT DEFAULT NULL COMMENT '掌上高考源站学校ID',
  `belong` VARCHAR(64) DEFAULT NULL COMMENT '主管部门 (如教育部/工信部/省教育厅)',
  `nature_name` VARCHAR(32) DEFAULT '公办' COMMENT '办学性质 (公办/民办)',
  `dual_class_name` VARCHAR(64) DEFAULT NULL COMMENT '双一流建设等级',
  `school_site` VARCHAR(255) DEFAULT NULL COMMENT '学校官方网站真实链接',
  `site` VARCHAR(255) DEFAULT NULL COMMENT '本科招生网真实链接',
  `phone` VARCHAR(128) DEFAULT NULL COMMENT '招生咨询联系电话',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '官方招生咨询邮箱',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '学校真实校区地址',
  `postcode` VARCHAR(32) DEFAULT NULL COMMENT '邮政编码',
  `ruanke_rank` INT DEFAULT NULL COMMENT '软科全国大学综合排名',
  `qs_rank` INT DEFAULT NULL COMMENT 'QS/国际大学综合排名',
  `xyh_rank` INT DEFAULT NULL COMMENT '校友会大学综合排名',
  `num_doctor` INT DEFAULT 0 COMMENT '一级博士学位授权点数量',
  `num_master` INT DEFAULT 0 COMMENT '一级硕士学位授权点数量',
  `num_academician` INT DEFAULT 0 COMMENT '两院院士人数',
  `num_library` VARCHAR(32) DEFAULT NULL COMMENT '图书馆藏书量',
  `num_lab` INT DEFAULT 0 COMMENT '重点科研实验室数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据入库时间',
  PRIMARY KEY (`id`),
  KEY `idx_province` (`province`),
  KEY `idx_school_type` (`school_type`),
  KEY `idx_school_level` (`school_level`),
  KEY `idx_school_name` (`school_name`),
  KEY `idx_raw_school_id` (`raw_school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全国高校基础事实信息表';

-- ---------------------------------------------------------
-- 3. 高校专业学科信息表 (major)
-- 作用: 记录高校开设的专业门类及近年平均就业率指标
-- 映射页面: 专业分析 (热力图/就业率 TOP10/新兴专业趋势)、高校详情优势专业分析
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '专业主键',
  `university_id` BIGINT NOT NULL COMMENT '关联高校主键 university.id',
  `major_name` VARCHAR(64) NOT NULL COMMENT '专业规范名称 (如: 人工智能, 计算机科学与技术)',
  `category` VARCHAR(32) NOT NULL COMMENT '学科门类: 工学 / 理学 / 医学 / 经济学 / 法学 / 管理学 / 文学等',
  `employment_rate` DECIMAL(5,2) DEFAULT '0.00' COMMENT '近年平均就业率百分比 (如: 96.50 表示 96.5%)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_univ_major` (`university_id`, `major_name`),
  KEY `idx_university_id` (`university_id`),
  KEY `idx_major_name` (`major_name`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高校专业学科设置与就业统计表';

-- ---------------------------------------------------------
-- 4. 历年招生与录取数据表 (enrollment)
-- 作用: 存储各高校历年招生计划数、实际录取人数与最低录取分数线
-- 映射页面: 招生分析 (报录比对比/批次漏斗/投放矩阵)、高校详情近五年录取分数走势
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `enrollment`;
CREATE TABLE `enrollment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '招生记录主键',
  `university_id` BIGINT NOT NULL COMMENT '关联高校主键 university.id',
  `year` INT NOT NULL COMMENT '招生年份 (如: 2020, 2021, 2022, 2023, 2024)',
  `province` VARCHAR(32) DEFAULT NULL COMMENT '生源省份 (如: 北京, 湖南, 河南)',
  `subject_type` VARCHAR(32) DEFAULT NULL COMMENT '招生科类 (如: 物理类, 历史类, 综合)',
  `plan_number` INT NOT NULL DEFAULT '0' COMMENT '计划招生总人数',
  `admission_number` INT NOT NULL DEFAULT '0' COMMENT '实际录取投档人数',
  `score` DECIMAL(5,2) NOT NULL DEFAULT '0.00' COMMENT '调档最低录取分数线 (理科/综合)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_univ_year_prov_sub` (`university_id`, `year`, `province`, `subject_type`),
  KEY `idx_univ_year` (`university_id`, `year`),
  KEY `idx_prov_sub` (`province`, `subject_type`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高校历年招生与录取分数事实表';

-- ---------------------------------------------------------
-- 5. 数据采集任务与日志表 (data_collect_log)
-- 作用: 记录多源分布式爬虫任务调度历史、数据源状态及吞吐量
-- 映射页面: 数据采集中心 (活跃数据源/采集流程/实时日志流/吞吐走势)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `data_collect_log`;
CREATE TABLE `data_collect_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '采集日志主键',
  `source_name` VARCHAR(128) NOT NULL COMMENT '数据源名称: 教育部公开数据网 / 各高校官方门户 / 全国招生阳光高考网 / 专业学科数据库',
  `collect_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集任务触发时间',
  `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '采集状态: SUCCESS(成功) / FAILED(失败) / RUNNING(进行中)',
  `data_count` INT NOT NULL DEFAULT '0' COMMENT '本次批次采集抓取数据条数',
  PRIMARY KEY (`id`),
  KEY `idx_collect_time` (`collect_time`),
  KEY `idx_source_name` (`source_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分布式数据采集调度与监控日志表';

-- ---------------------------------------------------------
-- 6. ETL 数据清洗与质量记录表 (data_clean_log)
-- 作用: 存储数据清洗流水线各批次的原始量、入库量、去重及异常修复统计
-- 映射页面: 数据清洗中心 (ETL 管道/清洗前后对比/质量指标比例/7日处理吞吐)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `data_clean_log`;
CREATE TABLE `data_clean_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '清洗批次主键',
  `raw_count` INT NOT NULL DEFAULT '0' COMMENT '清洗前原始湖仓日志量 (条)',
  `clean_count` INT NOT NULL DEFAULT '0' COMMENT '清洗校验后有效标准入库量 (条)',
  `error_count` INT NOT NULL DEFAULT '0' COMMENT '过滤重复、空值修复与异常修正数据量 (条)',
  `clean_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'ETL清洗执行完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_clean_time` (`clean_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ETL数据清洗与质量校验日志表';

-- ---------------------------------------------------------
-- 7. 机器学习预测结果表 (prediction_result)
-- 作用: 固化存储机器学习回归模型对高校总量、前沿专业增量及招生规模的未来推演数据
-- 映射页面: 趋势预测分析 (模型指标/高校数量推演/新兴专业增量预测/高考适龄生源预测)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS `prediction_result`;
CREATE TABLE `prediction_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预测记录主键',
  `type` VARCHAR(64) NOT NULL COMMENT '预测业务类型: UNIV_COUNT(高校总量) / MAJOR_AI(人工智能) / MAJOR_BIGDATA(大数据) / MAJOR_SE(软件工程) / ENROLL_TOTAL(高考录取总数) / POPULATION_LIMIT(适龄生源上限)',
  `year` INT NOT NULL COMMENT '推演目标年份 (如: 2025, 2026, 2027, 2028, 2030)',
  `predict_value` DECIMAL(10,2) NOT NULL COMMENT '模型推演数值 (如高校数/新增院校数/万人规模)',
  `generated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '本批次模型生成时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_year` (`type`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机器学习模型趋势推演预测结果表';

SET FOREIGN_KEY_CHECKS = 1;
