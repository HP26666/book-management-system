package com.bookms.service.impl;

import com.bookms.config.BookmsProperties;
import com.bookms.dto.response.FileUploadResponse;
import com.bookms.exception.BusinessException;
import com.bookms.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BookmsProperties bookmsProperties;

    @Override
    public FileUploadResponse upload(MultipartFile file) {
        validateFile(file);
        Path uploadDir = Path.of(bookmsProperties.getFile().getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
            String fileName = NAME_FORMATTER.format(LocalDateTime.now()) + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path target = uploadDir.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new FileUploadResponse(fileName, "/api/files/" + fileName, file.getSize());
        } catch (IOException exception) {
            throw new BusinessException(500, "文件保存失败");
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path uploadDir = Path.of(bookmsProperties.getFile().getUploadDir()).toAbsolutePath().normalize();
            Path file = uploadDir.resolve(filename).normalize();
            if (!file.startsWith(uploadDir)) {
                throw new BusinessException(400, "非法文件路径");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                throw new BusinessException(404, "文件不存在");
            }
            return resource;
        } catch (IOException exception) {
            throw new BusinessException(500, "文件读取失败");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        if (file.getSize() > bookmsProperties.getFile().getMaxSizeBytes()) {
            throw new BusinessException(400, "上传文件大小超过限制");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)
                || bookmsProperties.getFile().getAllowedExtensions().stream().noneMatch(allowed -> allowed.equalsIgnoreCase(extension))) {
            throw new BusinessException(400, "不支持的文件类型");
        }
    }
}