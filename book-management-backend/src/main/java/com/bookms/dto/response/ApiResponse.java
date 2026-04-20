package com.bookms.dto.response;

import com.bookms.config.TraceIdFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int code;

    private final String message;

    private final T data;

    private final String traceId;

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null, currentTraceId());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data, currentTraceId());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, currentTraceId());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, currentTraceId());
    }

    private static String currentTraceId() {
        return MDC.get(TraceIdFilter.TRACE_ID_KEY);
    }
}