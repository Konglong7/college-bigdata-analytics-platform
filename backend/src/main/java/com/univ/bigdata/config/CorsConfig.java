package com.univ.bigdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域（CORS）配置。
 *
 * <p><b>关键背景（一次真实踩坑记录）：</b>
 * 浏览器对 POST / PUT / DELETE 请求<strong>即使同源也会携带 Origin 头</strong>
 * （GET/HEAD 同源时不带）。因此如果把 CORS 来源写成一份严格白名单，
 * Spring 的 {@code CorsFilter} 会把本应用自己前端发来的登录请求也判定为
 * 「非法 CORS 请求」并直接返回 403，表现为线上「点击登录无反应 / 提示权限不足」。
 * 实测三种来源对照：无 Origin → 200；白名单内 Origin → 200；
 * 生产同源域名与任意其他 Origin → 403。
 *
 * <p><b>安全取舍：</b>
 * 真正让「通配来源」变得危险的是 {@code allowCredentials(true)}——
 * 它允许任意站点携带浏览器自动附带的凭据（Cookie / TLS 客户端证书）发起请求。
 * 本服务使用 Authorization: Bearer &lt;JWT&gt; 头进行鉴权，令牌保存在 localStorage，
 * 浏览器不会自动附加，跨站页面也无法读取其他源的 localStorage，
 * 因此这里显式关闭 CORS 凭据，保留通配来源以保证同源与本地分离开发都能正常工作。
 * 这是「可用性优先且不留真实凭据风险」的组合，而不是简单地把配置改回原样。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 不使用 Cookie 鉴权，无需 CORS 凭据：这是通配来源能够安全启用的前提
        config.setAllowCredentials(false);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        // 仅为 REST 接口注册跨域策略，静态资源无需处理
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
