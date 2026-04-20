package com.bookms.security;

import com.bookms.exception.BusinessException;
import java.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new BusinessException(401, "未登录");
        }
        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().id();
    }

    public static boolean hasAnyRole(String... roles) {
        CustomUserPrincipal principal = getCurrentUser();
        return Arrays.stream(roles).anyMatch(role -> principal.roles().contains(role.toLowerCase()));
    }
}