package com.bookms.service;

import com.bookms.dto.BookVO;

public interface IsbnService {
    BookVO lookupByIsbn(String isbn);
}
