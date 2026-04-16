package com.bookms.service;

import com.bookms.dto.PermissionTreeVO;

import java.util.List;

public interface PermissionService {
    List<PermissionTreeVO> getPermissionTree();
    List<PermissionTreeVO> getUserMenus(Long userId);
    List<String> getUserPermissionCodes(Long userId);
}
