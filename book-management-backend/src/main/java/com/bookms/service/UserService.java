package com.bookms.service;

import com.bookms.dto.*;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PageResponse<UserVO> listUsers(String keyword, Integer status, Pageable pageable);
    UserVO getUserById(Long id);
    UserVO createUser(UserCreateDTO dto);
    UserVO updateUser(Long id, UserUpdateDTO dto);
    void deleteUser(Long id);
    void updateUserStatus(Long id, Integer status);
    void resetPassword(Long id);
    UserVO getCurrentUser();
    void changePassword(String oldPassword, String newPassword);
}
