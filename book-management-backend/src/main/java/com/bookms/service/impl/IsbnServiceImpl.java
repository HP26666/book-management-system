package com.bookms.service.impl;

import com.bookms.dto.BookVO;
import com.bookms.service.IsbnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class IsbnServiceImpl implements IsbnService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public BookVO lookupByIsbn(String isbn) {
        try {
            String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.isEmpty()) {
                log.info("ISBN({})未查询到结果", isbn);
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> bookData = (Map<String, Object>) response.get("ISBN:" + isbn);
            if (bookData == null) {
                return null;
            }

            BookVO vo = new BookVO();
            vo.setIsbn(isbn);
            vo.setTitle((String) bookData.get("title"));

            if (bookData.get("authors") instanceof java.util.List<?> authors && !authors.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> firstAuthor = (Map<String, Object>) authors.get(0);
                vo.setAuthor((String) firstAuthor.get("name"));
            }

            if (bookData.get("publishers") instanceof java.util.List<?> publishers && !publishers.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> firstPublisher = (Map<String, Object>) publishers.get(0);
                vo.setPublisher((String) firstPublisher.get("name"));
            }

            log.info("ISBN({})查询成功: {}", isbn, vo.getTitle());
            return vo;
        } catch (Exception e) {
            log.warn("ISBN({})查询失败: {}", isbn, e.getMessage());
            return null;
        }
    }
}
