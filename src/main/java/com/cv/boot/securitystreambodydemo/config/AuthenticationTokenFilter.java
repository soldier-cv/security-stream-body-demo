package com.cv.boot.securitystreambodydemo.config;

import cn.hutool.core.util.StrUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 认证过滤器
 *
 * @author bing.xun
 */
@Component
@Slf4j
@AllArgsConstructor
public class AuthenticationTokenFilter extends OncePerRequestFilter {
    private final TokenStoreCache tokenStoreCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        log.info("AuthenticationTokenFilter 开始执行, 调度类型: {}, requestURI: {}", request.getDispatcherType(), request.getRequestURI());

        String accessToken = request.getHeader("X-Auth-Token");
        if (StrUtil.isBlank(accessToken)) {
            accessToken = request.getParameter("X-Auth-Token");
        }

        // accessToken为空，表示未登录
        if (StringUtils.isBlank(accessToken)) {
            log.warn("未找到 'X-Auth-Token'。当前认证状态: {}", SecurityContextHolder.getContext().getAuthentication());

            chain.doFilter(request, response);
            return;
        }

        // 获取登录用户信息
        UserDetail user = tokenStoreCache.getUser(accessToken);
        log.info("找到有效的 'X-Auth-Token', accessToken: {}", accessToken);

        if (user == null) {
            log.warn("未找到用户信息。当前认证状态: {}", SecurityContextHolder.getContext().getAuthentication());
            chain.doFilter(request, response);
            return;
        }

        // 用户存在
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // 新建 SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        chain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 如果是公共资源请求，则返回 true (表示“不应该过滤”)
        if ("/favicon.ico".equals(uri) || uri.startsWith("/.well-known")) {
            return true;
        }
        return false;
    }
}
