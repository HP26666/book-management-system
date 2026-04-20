package com.bookms.service;

import com.bookms.entity.OperationLog;

public interface OperationLogService {

    void saveAsync(OperationLog operationLog);
}