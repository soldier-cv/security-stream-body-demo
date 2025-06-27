package com.cv.boot.securitystreambodydemo.config;

import org.springframework.stereotype.Component;

/**
 * 这是一个模拟的Token存储和验证组件。
 * 在真实项目中，这里会连接 Redis 或数据库来验证Token并获取用户信息。
 * 在我们的Demo中，它只识别一个固定的有效Token。
 */
@Component
public class TokenStoreCache {

    public UserDetail getUser(String accessToken) {
        // 如果Token是 "valid-secret-token"，我们就认为它有效
        if ("valid-secret-token".equals(accessToken)) {
            // 返回一个固定的、写死的用户信息
            return new UserDetail("token-user", "ROLE_USER");
        }
        // 对于任何其他token，都认为是无效的
        return null;
    }
}
