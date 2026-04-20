package com.bookms.security;

import com.bookms.entity.User;
import com.bookms.exception.BusinessException;
import com.bookms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return CustomUserPrincipal.fromUser(findDomainUserByUsername(username));
    }

    public CustomUserPrincipal loadPrincipalByUserId(Long userId) {
        return CustomUserPrincipal.fromUser(findDomainUserById(userId));
    }

    public User findDomainUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    public User findDomainUserById(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
}