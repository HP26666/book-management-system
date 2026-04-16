package com.bookms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer level;
    private List<CategoryTreeVO> children;
}
