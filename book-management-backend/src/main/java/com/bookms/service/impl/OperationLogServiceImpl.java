package com.bookms.service.impl;

import com.bookms.entity.OperationLog;
import com.bookms.repository.OperationLogRepository;
import com.bookms.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Override
    @Async
    public void saveAsync(OperationLog operationLog) {
        operationLogRepository.save(operationLog);
    }
}