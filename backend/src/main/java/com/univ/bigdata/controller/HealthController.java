package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务健康检查端点。
 *
 * <p>为什么需要它：Render 默认只做 TCP 端口探测，无法区分「进程活着」与
 * 「应用真的可用」。此前线上出现过一种典型故障——Spring 上下文启动成功、
 * 端口可连接，但 H2 建表尚未完成（或外部数据源连不上），
 * 所有接口都返回「Table not found」，而平台侧显示服务健康。
 *
 * <p>因此这里在返回进程状态的同时，真实执行一次业务表探测查询，
 * 让保活脚本与部署平台能够发现「接口全 500 但进程存活」这类假健康状态。
 *
 * <p>注意：本接口始终返回 HTTP 200，数据库异常信息放在响应体的 database 字段中。
 * 这样设计的目的是让运维侧可以读取诊断信息，同时避免部署平台因健康检查失败
 * 而反复重启实例（免费实例重启一次需要 30~60 秒，对演示体验是负收益）。
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", "univ-bigdata-backend");
        data.put("timestamp", System.currentTimeMillis());

        try {
            Integer rows = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM university", Integer.class);
            data.put("database", "UP");
            data.put("universityRows", rows);
            data.put("ready", rows != null && rows > 0);
        } catch (Exception ex) {
            // 表不存在 / 数据源不可达：数据层尚未就绪，明确暴露出来而不是伪装成健康
            data.put("database", "DOWN");
            data.put("ready", false);
            data.put("databaseError", ex.getMessage());
        }

        return Result.success(data);
    }
}
