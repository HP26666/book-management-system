package com.bookms.service;

import com.bookms.dto.request.ReaderSaveRequest;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.ReaderResponse;
import com.bookms.entity.Reader;
import com.bookms.entity.User;

public interface ReaderService {

    PageResponse<ReaderResponse> pageReaders(String keyword, String readerType, int page, int size);

    ReaderResponse getReader(Long id);

    ReaderResponse create(ReaderSaveRequest request);

    ReaderResponse update(Long id, ReaderSaveRequest request);

    ReaderResponse issueCard(Long id);

    ReaderResponse currentReader();

    Reader createDefaultReader(User user);
}