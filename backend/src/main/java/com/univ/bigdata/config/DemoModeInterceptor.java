package com.univ.bigdata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.univ.bigdata.common.api.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Set;

/**
 * 演示模式拦截器
 * 生产演示环境下拦截对核心高校、专业、录取数据的破坏性写入/删除请求
 */
@Slf4j
@Component
public class DemoModeInterceptor implements HandlerInterceptor {

    @Value("${univ.demo-mode:false}")
    private boolean demoMode;

    private static final Set<String> ALLOWED_WRITE_PREFIXES = Set.of(
            "/api/auth/",
            "/api/recommend/",
            "/api/predict/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!demoMode) {
            return true;
        }

        String method = request.getMethod();
        // GET 和 OPTIONS 请求全部放行
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String uri = request.getRequestURI();
        // 允许白名单内的交互写入（如登录认证、智能志愿推荐计算、在线分数预测）
        for (String prefix : ALLOWED_WRITE_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }

        // 拦截对核心数据和管理员配置的破坏性操作
        log.warn("[DemoMode] 拦截演示模式下的写操作: method={}, uri={}", method, uri);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter writer = response.getWriter()) {
            Result<Void> errorResult = Result.failed(403, "当前处于在线演示模式，系统基准数据受保护，禁止新增、修改或删除操作");
            writer.write(new ObjectMapper().writeValueAsString(errorResult));
            writer.flush();
        }
        return false;
    }
}
