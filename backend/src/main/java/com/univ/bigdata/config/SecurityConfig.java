package com.univ.bigdata.config;

import com.univ.bigdata.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行前端静态资源与 SPA 单页路由
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/china.json", "/vite.svg", "/assets/**").permitAll()
                        .requestMatchers(request -> !request.getServletPath().startsWith("/api/")).permitAll()
                        // 2. 放行认证相关接口
                        .requestMatchers("/api/auth/**").permitAll()
                        // 3. 放行所有大屏展示与公开数据查询 GET 接口
                        .requestMatchers(HttpMethod.GET,
                                "/api/dashboard/**",
                                "/api/university/**",
                                "/api/major/**",
                                "/api/enrollment/**",
                                "/api/collect/**",
                                "/api/clean/**",
                                "/api/predict/**",
                                "/api/warehouse/**"
                        ).permitAll()
                        // 4. 放行志愿推荐与测算匹配接口
                        .requestMatchers("/api/recommend/**").permitAll()
                        // 5. 后台管理员接口需 ROLE_ADMIN
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        // 6. 其余请求需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
