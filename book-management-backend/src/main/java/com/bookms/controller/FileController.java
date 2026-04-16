package com.bookms.controller;

import com.bookms.dto.ApiResponse;
import com.bookms.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(fileService.uploadImage(file));
    }

    @Operation(summary = "删除图片")
    @DeleteMapping
    public ApiResponse<Void> deleteImage(@RequestParam String path) {
        fileService.deleteImage(path);
        return ApiResponse.ok(null);
    }
}
