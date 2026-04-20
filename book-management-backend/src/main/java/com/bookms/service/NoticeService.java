package com.bookms.service;

import com.bookms.dto.request.NoticeSaveRequest;
import com.bookms.dto.response.NoticeResponse;
import com.bookms.dto.response.PageResponse;

public interface NoticeService {

    PageResponse<NoticeResponse> pageNotices(Short type, Short status, int page, int size, boolean manageView);

    NoticeResponse getNotice(Long id, boolean manageView);

    NoticeResponse create(NoticeSaveRequest request);

    NoticeResponse update(Long id, NoticeSaveRequest request);

    void delete(Long id);
}