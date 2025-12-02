package com.jingdezhen.tourism.file.controller;

import com.jingdezhen.tourism.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传Controller
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${file.upload.base-url:http://localhost:8080/api/file/}")
    private String baseUrl;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }

            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String fileName = UUID.randomUUID().toString() + extension;

            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            Path filePath = Paths.get(uploadPath, fileName);
            Files.write(filePath, file.getBytes());

            String fileUrl = baseUrl + fileName;
            log.info("文件上传成功: {}", fileUrl);

            return Result.success("上传成功", fileUrl);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件
     */
    @GetMapping("/{filename}")
    public org.springframework.http.ResponseEntity<byte[]> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            byte[] fileContent = Files.readAllBytes(filePath);
            
            String contentType = "application/octet-stream";
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerFilename.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerFilename.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerFilename.endsWith(".webp")) {
                contentType = "image/webp";
            }
            
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "max-age=86400")
                    .body(fileContent);
        } catch (IOException e) {
            log.error("文件读取失败: {}", filename, e);
            return org.springframework.http.ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{filename}")
    public Result<Void> deleteFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filename);
            return Result.success("删除成功");
        } catch (IOException e) {
            log.error("文件删除失败: {}", filename, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}

