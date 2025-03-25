package com.hui.ai.task;

import com.hui.ai.constants.VectorStoreIdConstants;
import com.hui.ai.repository.ChatHistoryRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.expression.Expression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ValueRange;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHistoryTask {
    private final ChatHistoryRepository chatHistoryRepository;
    private final FileStorageService fileStorageService;
    private final VectorStore vectorStore;

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteAll() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("清空历史聊天记录");
        chatHistoryRepository.deleteAll();
        new File("chat-pdf.json").delete();

        try {
            // 删除vector store数据
            vectorStore.delete(VectorStoreIdConstants.documentIdList);
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minio", "minio123")
                .build();

        // 清空minio pdf
        String bucketName = "file";
        List<DeleteObject> objectsToDelete = new LinkedList<>();
        ListObjectsArgs listArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .includeVersions(true)  // 包含所有版本
                .build();

        for (Result<Item> result : minioClient.listObjects(listArgs)) {
            Item item = result.get();
            objectsToDelete.add(new DeleteObject(item.objectName(), item.versionId()));
            System.out.println("待删除对象: " + item.objectName() + " (版本: " + item.versionId() + ")");
        }

        // 批量删除所有对象及版本
        if (!objectsToDelete.isEmpty()) {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objectsToDelete)
                            .build()
            );

            // 检查删除错误
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                System.err.println("删除失败: " + error.objectName() + " - " + error.message());
            }
        }

    }

}
