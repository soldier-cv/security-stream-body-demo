package com.cv.boot.securitystreambodydemo.config;

// 我们不再使用 PathRequest，所以可以移除这个导入
// import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.request.async.SecurityContextCallableProcessingInterceptor;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.registerCallableInterceptors(new SecurityContextCallableProcessingInterceptor());
    }



    /**
     * 解决 .well-known 路径中包含 // 导致请求被拒绝的问题
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationTokenFilter tokenFilter) throws Exception {
        http
                // 添加异步处理的集成过滤器
                .addFilter(new WebAsyncManagerIntegrationFilter())

                // 添加您的自定义Token过滤器
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)

                // 禁用 CSRF 和 Session
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置接口权限
                .authorizeHttpRequests(auth -> auth
                        // 因为公共资源已在 webSecurityCustomizer 中被忽略，这里就不再需要为它们配置 permitAll()
                        .requestMatchers("/ping").hasRole("USER")
                        .requestMatchers("/download").hasRole("USER")
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
