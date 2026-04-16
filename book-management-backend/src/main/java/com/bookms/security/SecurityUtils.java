package com.bookms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserPrincipal principal = getCurrentUser();
        if (principal == null) {
            throw new RuntimeException("未登录");
        }
        return principal.getUserId();
    }

    public static Long getCurrentUserIdOrNull() {
        UserPrincipal principal = getCurrentUser();
        return principal != null ? principal.getUserId() : null;
    }

    public static String getCurrentUsername() {
        UserPrincipal principal = getCurrentUser();
        return principal != null ? principal.getUsername() : null;
    }

    public static boolean hasRole(String role) {
        UserPrincipal principal = getCurrentUser();
        return principal != null && principal.getRoles().contains(role);
    }
}
