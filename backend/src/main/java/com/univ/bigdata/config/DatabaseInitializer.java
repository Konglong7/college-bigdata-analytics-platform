package com.univ.bigdata.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 数据库自动初始化与基准数据装载器
 * 1. 针对云原生演示环境（如 Render Free Tier），支持无外部 MySQL 时自动通过嵌入式库秒级播种数据；
 * 2. 针对外部 TiDB / MySQL 环境，支持首次空库自建表与自初始化，无需手动执行繁琐的 DDL/DML 脚本；
 * 3. 若已有数据则自动跳过，严格保障数据幂等与安全性。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        boolean needInit = false;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM university", Integer.class);
            if (count == null || count == 0) {
                needInit = true;
            } else {
                log.info("数据库已就绪，当前高校基准记录数: {}", count);
            }
        } catch (Exception e) {
            log.info("未检测到有效数据库表结构，准备执行数据库自动建表与基准数据装载...");
            needInit = true;
        }

        if (needInit) {
            try {
                log.info("开始执行数据库表结构与基准数据装载...");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("db/schema.sql"));
                populator.addScript(new ClassPathResource("db/data.sql"));
                populator.setContinueOnError(true);
                populator.execute(dataSource);
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM university", Integer.class);
                log.info("数据库自动装载完成！高校记录数: {}", count);
            } catch (Exception e) {
                log.error("数据库自动装载异常: {}", e.getMessage(), e);
            }
        }
    }
}
