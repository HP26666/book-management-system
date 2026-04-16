package com.bookms.security;

import com.bookms.entity.SysUser;
import com.bookms.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleCode())
                .toList();

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), roles);
        principal.setPassword(user.getPassword());
        principal.setEnabled(user.getStatus() == 1);
        return principal;
    }
}
