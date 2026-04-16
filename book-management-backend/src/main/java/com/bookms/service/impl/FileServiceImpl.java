package com.bookms.service.impl;

import com.bookms.exception.BusinessException;
import com.bookms.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${bookms.upload.path:./uploads}")
    private String uploadPath;

    @Value("${bookms.upload.max-size:5242880}")
    private long maxSize;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持 jpg/png/gif/webp");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(filename).normalize();

            // Security: ensure target is within upload directory
            if (!targetPath.startsWith(uploadDir)) {
                throw new BusinessException("非法文件路径");
            }

            file.transferTo(targetPath.toFile());
            log.info("文件上传成功: {}", filename);
            return "/uploads/" + filename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String filePath) {
        if (filePath == null || filePath.isBlank()) return;

        String filename = filePath.replace("/uploads/", "");
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path targetPath = uploadDir.resolve(filename).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new BusinessException("非法文件路径");
            }

            Files.deleteIfExists(targetPath);
            log.info("文件删除成功: {}", filename);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", filePath, e);
        }
    }
}
