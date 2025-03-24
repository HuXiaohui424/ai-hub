package com.hui.ai.repository.impl;

import com.hui.ai.repository.FileRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReaderInputStream;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.FileNameMap;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinIORepository implements FileRepository {

    private final VectorStore vectorStore;
    private final FileStorageService fileStorageService;

    // 会话id和文件的映射关系
    private final Properties chatProps = new Properties();


    @Override
    public boolean save(String chatId, MultipartFile file) {
        //手动构造文件信息
        FileInfo fileInfo = new FileInfo()
                .setPlatform("minio-1")
                .setBasePath("pdf/")
                .setFilename(file.getOriginalFilename());
        boolean exists = fileStorageService.exists(fileInfo);
        if (exists) {
            // 上传失败，文件存在
            log.error("文件上传失败，文件已存在");
            return false;
        }

        // 上传minio
        fileStorageService.of(file)
                .setSaveFilename(fileInfo.getFilename())
                .upload();

        // 保存映射关系
        chatProps.put(chatId, fileInfo.getFilename());
        return true;
    }

    @Override
    public Resource get(String chatId) {
        String filename = chatProps.getProperty(chatId);
        FileInfo fileInfo = new FileInfo()
                .setPlatform("minio-1")
                .setBasePath("pdf/")
                .setFilename(filename);
        //下载
        byte[] bytes = fileStorageService.download(fileInfo).bytes();

        // 将字节转换为Resource对象
        return new NameWithByteArrayResource(bytes, filename);
    }

    @PostConstruct
    public void initial() throws IOException {
        try {
            // 加载
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.load(new File("chat-pdf.json"));

            FileInfo fileInfo = new FileInfo()
                    .setPlatform("minio-1")
                    .setBasePath("pdf/")
                    .setFilename("chat-pdf.properties");

            byte[] bytes = fileStorageService.download(fileInfo).bytes();
            chatProps.load(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        // 持久化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        chatProps.store(bos, String.valueOf(LocalDateTime.now()));
        // 转换为输入流
        byte[] data = bos.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        fileStorageService.of(inputStream)
                .setSaveFilename("chat-pdf.properties")
                .upload();

        SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;

        // 向量数据库保存（simpleVectorStore仅用作学习或教学使用，生产环境请使用spring ai支持的向量数据库，默认持久化）
        simpleVectorStore.save(new File("chat-pdf.json"));
    }
}
