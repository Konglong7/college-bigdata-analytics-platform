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

    /**
     * 演示模式下仍允许的写操作。
     *
     * <p>此前使用的是前缀白名单 {@code "/api/auth/"}，导致
     * {@code POST /api/auth/register} 在公网演示环境可被匿名无限调用，
     * 往数据库里灌入任意数量的 ROLE_USER 账号（512MB 免费实例可被直接打爆）。
     * 现改为精确路径白名单：只放行登录本身，注册在演示模式下被拒绝。
     */
    private static final Set<String> ALLOWED_WRITE_URIS = Set.of(
            "/api/auth/login"
    );

    /** 允许的交互式计算类前缀（志愿推荐测算与在线分数预测，无持久化副作用） */
    private static final Set<String> ALLOWED_WRITE_PREFIXES = Set.of(
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

        // 精确路径白名单：登录认证
        if (ALLOWED_WRITE_URIS.contains(uri)) {
            return true;
        }

        // 前缀白名单：智能志愿推荐计算、在线分数预测等无副作用的交互式写入
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
