package com.bookms.mapper;

import com.bookms.dto.request.NoticeSaveRequest;
import com.bookms.dto.response.NoticeResponse;
import com.bookms.entity.Notice;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface NoticeMapper {

    @Mapping(target = "publisherId", source = "publisher.id")
    @Mapping(target = "publisherName", expression = "java(notice.getPublisher() == null ? null : (notice.getPublisher().getRealName() != null ? notice.getPublisher().getRealName() : notice.getPublisher().getUsername()))")
    NoticeResponse toResponse(Notice notice);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notice toEntity(NoticeSaveRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(NoticeSaveRequest request, @MappingTarget Notice notice);
}