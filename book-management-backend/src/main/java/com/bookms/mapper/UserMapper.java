package com.bookms.mapper;

import com.bookms.dto.request.UserCreateRequest;
import com.bookms.dto.request.UserUpdateRequest;
import com.bookms.dto.response.RoleResponse;
import com.bookms.dto.response.UserInfoResponse;
import com.bookms.dto.response.UserResponse;
import com.bookms.entity.Permission;
import com.bookms.entity.Role;
import com.bookms.entity.User;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(toRoleResponses(user.getRoles()))")
    UserResponse toResponse(User user);

    @Mapping(target = "roles", expression = "java(toRoleCodes(user.getRoles()))")
    @Mapping(target = "permissions", expression = "java(toPermissionCodes(user.getRoles()))")
    UserInfoResponse toUserInfo(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "reader", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "reader", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    default List<RoleResponse> toRoleResponses(Set<Role> roles) {
        return roles.stream()
                .sorted(Comparator.comparing(Role::getRoleCode))
                .map(role -> {
                    RoleResponse response = new RoleResponse();
                    response.setId(role.getId());
                    response.setRoleCode(role.getRoleCode());
                    response.setRoleName(role.getRoleName());
                    return response;
                })
                .toList();
    }

    default List<String> toRoleCodes(Set<Role> roles) {
        return roles.stream()
                .map(Role::getRoleCode)
                .sorted()
                .toList();
    }

    default List<String> toPermissionCodes(Set<Role> roles) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getPermCode)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}