package com.univ.bigdata.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
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
 *
 * <p><b>为什么实现 InitializingBean 而不是 CommandLineRunner：</b>
 * Spring Boot 的启动顺序是「先启动内嵌 Tomcat 接受连接，再回调 CommandLineRunner」。
 * 此前本类实现 CommandLineRunner，导致线上存在一个真实可复现的竞态窗口：
 * 端口已经可以响应请求，但 university 表尚未创建，此时所有接口都会抛
 * {@code Table "university" not found}，前端首屏因此渲染成一屏 0 数据
 * （实测本地窗口约 0.9 秒，Render 0.1 核免费实例上更长）。
 * 改为 {@link InitializingBean#afterPropertiesSet()} 后，建表灌数发生在容器
 * 刷新阶段、Tomcat 监听端口之前，服务「一旦可访问即数据可用」。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements InitializingBean {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() {
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

        if (!needInit) {
            return;
        }

        try {
            log.info("开始执行数据库表结构与基准数据装载...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/schema.sql"));
            populator.addScript(new ClassPathResource("db/data.sql"));
            // 保留 continueOnError：schema/data 脚本需同时兼容 H2(MODE=MySQL) 与真实 MySQL 的方言差异
            populator.setContinueOnError(true);
            populator.execute(dataSource);

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM university", Integer.class);
            if (count == null || count == 0) {
                // 不抛异常中断启动，但必须留下明确的高优先级日志，
                // 否则会再次退化成「进程健康、接口全 500」的假健康状态
                log.error("数据库自动装载后 university 表仍为空，请检查 db/schema.sql 与 db/data.sql 的方言兼容性");
            } else {
                log.info("数据库自动装载完成！高校记录数: {}", count);
            }
        } catch (Exception e) {
            log.error("数据库自动装载异常: {}", e.getMessage(), e);
        }
    }
}
