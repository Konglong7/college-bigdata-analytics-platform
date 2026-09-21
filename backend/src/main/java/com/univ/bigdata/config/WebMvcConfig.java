package com.univ.bigdata.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Web MVC 配置
 * 1. 注册演示模式拦截器
 * 2. 托管前端静态资源（带缓存优化与预压缩支持）
 * 3. 前端 Vue SPA 路由 fallback 转发至 index.html
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DemoModeInterceptor demoModeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(demoModeInterceptor)
                .addPathPatterns("/api/**")
                .order(0);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Vite 静态构建产物 (/assets/** 带 Hash)，配置 1 年强缓存与 Gzip 预压缩匹配
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());

        // 2. 地图数据、图标等常用静态文件配置 7 天缓存
        registry.addResourceHandler("/favicon.ico", "/china.json", "/vite.svg")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .resourceChain(true);

        // 3. 通用静态资源与 SPA 路由 Fallback
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // 如果是后端 API 接口，不拦截，交给对应 Controller
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }

                        // 其余前端页面路由统一转发至 index.html (SPA History 模式)
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
