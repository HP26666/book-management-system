package com.bookms.mapper;

import com.bookms.dto.response.BorrowResponse;
import com.bookms.entity.BorrowRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "realName", source = "user.realName")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "coverUrl", source = "book.coverUrl")
    @Mapping(target = "status", expression = "java(record.getStatus() == null ? null : record.getStatus().getCode())")
    @Mapping(target = "approveUserId", source = "approveUser.id")
    @Mapping(target = "approveUserName", expression = "java(record.getApproveUser() == null ? null : (record.getApproveUser().getRealName() != null ? record.getApproveUser().getRealName() : record.getApproveUser().getUsername()))")
    BorrowResponse toResponse(BorrowRecord record);
}