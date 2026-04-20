package com.bookms.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {

    private Long id;

    private String name;

    private Long parentId;

    private Integer sortOrder;

    private Integer level;

    private List<CategoryResponse> children = new ArrayList<>();
}