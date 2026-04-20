package com.bookms.security;

import com.bookms.entity.Permission;
import com.bookms.entity.Role;
import com.bookms.entity.User;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserPrincipal(
        Long id,
        String username,
        String password,
        String realName,
        Short status,
        Set<String> roles,
        Set<String> permissions,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    public static CustomUserPrincipal fromUser(User user) {
        Set<String> roleCodes = new LinkedHashSet<>();
        Set<String> permissionCodes = new LinkedHashSet<>();
        Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();

        for (Role role : user.getRoles()) {
            String roleCode = role.getRoleCode().toUpperCase();
            roleCodes.add(role.getRoleCode());
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
            for (Permission permission : role.getPermissions()) {
                if (permission.getPermCode() != null) {
                    permissionCodes.add(permission.getPermCode());
                    grantedAuthorities.add(new SimpleGrantedAuthority(permission.getPermCode()));
                }
            }
        }

        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getStatus(),
                roleCodes,
                permissionCodes,
                List.copyOf(grantedAuthorities));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return status != null && status == 1;
    }
}