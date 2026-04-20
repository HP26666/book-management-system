package com.bookms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateRequest {

    @NotNull(message = "bookId 不能为空")
    private Long bookId;
}