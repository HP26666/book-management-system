package com.bookms.mapper;

import com.bookms.dto.response.ReservationResponse;
import com.bookms.entity.ReservationRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "realName", source = "user.realName")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "status", expression = "java(record.getStatus() == null ? null : record.getStatus().getCode())")
    ReservationResponse toResponse(ReservationRecord record);
}