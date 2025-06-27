package com.cv.boot.securitystreambodydemo.controller;

import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;

@RestController
public class DownloadController {

    private static final Logger log = LoggerFactory.getLogger(DownloadController.class);

    @GetMapping("/ping")
    public String ping(Principal principal) {
        // 一个简单的受保护接口，用于测试基础认证是否成功
        log.info("'/ping' 接口被调用，认证用户为: {}", principal.getName());
        return "pong! 您好, " + principal.getName();
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadFile() throws IOException {
        // 在开始下载前，打印当前的安全上下文信息
        log.info("'/download' 接口被调用 (REQUEST 阶段), 用户: {}",
                SecurityContextHolder.getContext().getAuthentication().getName());

        // 1. 创建一个临时文件用于演示
        Path tempFile = Files.createTempFile("demo-", ".txt");
        FileUtil.writeUtf8String("这是一个用于演示 StreamingResponseBody 的文件。", tempFile.toFile());
        log.info("创建了临时文件: {}", tempFile.toAbsolutePath());

        // 2. 准备流式响应体
        StreamingResponseBody responseBody = outputStream -> {
            // 这个 Lambda 表达式会在一个单独的线程中执行
            log.info("StreamingResponseBody 开始执行 (ASYNC 阶段), 用户: {}",
                    SecurityContextHolder.getContext().getAuthentication().getName());
            try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                log.info("文件流写入完成。");
            } finally {
                // 3. 在结束后删除临时文件
                Files.delete(tempFile);
                log.info("删除了临时文件: {}", tempFile.toAbsolutePath());
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=demo.txt");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(Files.size(tempFile))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }
}
