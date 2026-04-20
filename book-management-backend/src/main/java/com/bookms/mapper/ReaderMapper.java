package com.bookms.mapper;

import com.bookms.dto.request.ReaderSaveRequest;
import com.bookms.dto.response.ReaderResponse;
import com.bookms.entity.Reader;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ReaderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "realName", source = "user.realName")
    @Mapping(target = "isBlacklist", source = "blacklist")
    ReaderResponse toResponse(Reader reader);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "readerCardNo", ignore = true)
    @Mapping(target = "currentBorrowCount", ignore = true)
    @Mapping(target = "creditScore", ignore = true)
    @Mapping(target = "blacklist", source = "isBlacklist")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reader toEntity(ReaderSaveRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "readerCardNo", ignore = true)
    @Mapping(target = "currentBorrowCount", ignore = true)
    @Mapping(target = "creditScore", ignore = true)
    @Mapping(target = "blacklist", source = "isBlacklist")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ReaderSaveRequest request, @MappingTarget Reader reader);
}