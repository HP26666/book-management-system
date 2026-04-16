package com.bookms.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadImage(MultipartFile file);
    void deleteImage(String filePath);
}
