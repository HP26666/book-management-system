package com.bookms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadResponse {

    private String fileName;

    private String fileUrl;

    private long fileSize;
}