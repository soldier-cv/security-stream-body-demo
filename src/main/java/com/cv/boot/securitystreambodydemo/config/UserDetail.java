package com.cv.boot.securitystreambodydemo.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// 这是一个简单的 UserDetails 实现，用于在安全上下文中存储用户信息
public class UserDetail implements UserDetails {

    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetail(String username, String role) {
        this.username = username;
        this.authorities = CollUtil.newHashSet(new SimpleGrantedAuthority(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Token认证不需要密码
    }

    @Override
    public String getUsername() {
        return "user_from_token"; // 返回一个固定的用户名
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
