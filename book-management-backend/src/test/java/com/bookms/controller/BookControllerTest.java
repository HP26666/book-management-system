package com.bookms.controller;

import com.bookms.dto.*;
import com.bookms.security.JwtTokenProvider;
import com.bookms.service.BookService;
import com.bookms.service.IsbnService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private IsbnService isbnService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    private BookVO sampleBookVO;

    @BeforeEach
    void setUp() {
        sampleBookVO = BookVO.builder()
                .id(1L)
                .isbn("978-7-111-11111-1")
                .title("Java编程思想")
                .author("Bruce Eckel")
                .publisher("机械工业出版社")
                .totalStock(10)
                .availableStock(8)
                .build();
    }

    @Test
    @DisplayName("GET /books - 分页查询图书")
    void listBooks() throws Exception {
        PageResponse<BookVO> page = PageResponse.of(List.of(sampleBookVO), 1, 1, 10);
        when(bookService.listBooks(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/books")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("Java编程思想"));
    }

    @Test
    @DisplayName("GET /books/{id} - 获取图书详情")
    void getBookDetail() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(sampleBookVO);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isbn").value("978-7-111-11111-1"));
    }

    @Test
    @DisplayName("POST /books - 创建图书")
    void createBook() throws Exception {
        when(bookService.createBook(any(BookCreateDTO.class))).thenReturn(sampleBookVO);

        BookCreateDTO dto = new BookCreateDTO();
        dto.setIsbn("978-7-111-11111-1");
        dto.setTitle("Java编程思想");
        dto.setAuthor("Bruce Eckel");
        dto.setPublisher("机械工业出版社");
        dto.setCategoryId(1L);
        dto.setTotalStock(10);
        dto.setPrice(BigDecimal.valueOf(108.00));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /books/{id} - 删除图书")
    void deleteBook() throws Exception {
        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
