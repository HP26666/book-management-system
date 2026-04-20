package com.bookms.aspect;

import com.bookms.annotation.OperationLog;
import com.bookms.security.CustomUserPrincipal;
import com.bookms.service.OperationLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_PARAM_LENGTH = 1000;

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable throwable = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            saveLog(joinPoint, operationLog, start, throwable);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, long start, Throwable throwable) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();

        com.bookms.entity.OperationLog entity = new com.bookms.entity.OperationLog();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal principal) {
            entity.setUserId(principal.id());
            entity.setUsername(principal.username());
        }
        entity.setOperation(operationLog.value());
        entity.setMethod(joinPoint.getSignature().toShortString());
        entity.setRequestUri(request == null ? null : request.getRequestURI());
        entity.setIp(resolveIp(request));
        entity.setParams(truncate(serializeArgs(joinPoint.getArgs())));
        entity.setDurationMs(System.currentTimeMillis() - start);
        entity.setStatus((short) (throwable == null ? 1 : 0));
        entity.setErrorMsg(throwable == null ? null : throwable.getMessage());
        operationLogService.saveAsync(entity);
    }

    private String serializeArgs(Object[] args) {
        try {
            return objectMapper.writeValueAsString(Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest)
                            && !(arg instanceof HttpServletResponse)
                            && !(arg instanceof MultipartFile))
                    .collect(Collectors.toList()));
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private String truncate(String content) {
        if (content == null || content.length() <= MAX_PARAM_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_PARAM_LENGTH);
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}