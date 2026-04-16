package com.bookms.aspect;

import com.bookms.entity.SysOperationLog;
import com.bookms.repository.SysOperationLogRepository;
import com.bookms.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogRepository logRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog logEntry = new SysOperationLog();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                logEntry.setUserId(principal.getUserId());
                logEntry.setUsername(principal.getUsername());
            }

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logEntry.setRequestUri(request.getRequestURI());
                logEntry.setIp(getClientIp(request));
            }

            logEntry.setOperation(operationLog.description());
            logEntry.setMethod(joinPoint.getSignature().toShortString());

            // Only serialize simple params, skip file uploads
            try {
                MethodSignature sig = (MethodSignature) joinPoint.getSignature();
                String[] paramNames = sig.getParameterNames();
                Object[] args = joinPoint.getArgs();
                if (paramNames != null && paramNames.length > 0) {
                    StringBuilder params = new StringBuilder("{");
                    for (int i = 0; i < paramNames.length; i++) {
                        if (args[i] != null && !args[i].getClass().getName().contains("Multipart")) {
                            if (params.length() > 1) params.append(", ");
                            params.append(paramNames[i]).append("=");
                            String val = objectMapper.writeValueAsString(args[i]);
                            if (val.length() > 500) val = val.substring(0, 500) + "...";
                            params.append(val);
                        }
                    }
                    params.append("}");
                    logEntry.setParams(params.toString());
                }
            } catch (Exception ignored) {}

            Object result = joinPoint.proceed();

            logEntry.setStatus(1);
            logEntry.setDurationMs(System.currentTimeMillis() - startTime);
            logRepository.save(logEntry);

            return result;
        } catch (Throwable e) {
            logEntry.setStatus(0);
            logEntry.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000)) : "");
            logEntry.setDurationMs(System.currentTimeMillis() - startTime);
            logRepository.save(logEntry);
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
