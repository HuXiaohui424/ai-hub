package com.hui.ai.controller;

import com.hui.ai.entity.vo.ResultVO;
import com.hui.ai.repository.ChatHistoryRepository;
import com.hui.ai.repository.FileRepository;
import com.hui.ai.repository.impl.NameWithByteArrayResource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hui.ai.constants.VectorStoreIdConstants.documentIdList;

@RestController
@RequiredArgsConstructor
@RequestMapping("ai/pdf")
public class PdfController {
    private final FileRepository fileRepository;
    private final VectorStore vectorStore;
    private final ChatHistoryRepository historyRepository;
    private final ChatClient pdfChatClient;


    @PostMapping("/upload/{chatId}")
    public ResultVO update(@PathVariable("chatId") String chatId, MultipartFile file) {
        // 校验pdf
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResultVO.error("请上传pdf文件");
        }
        // 保存文件
        boolean success = fileRepository.save(chatId, file);
        if (!success) {
            return ResultVO.error("上传失败");
        }

        // 写入向量数据库
        writeToVectorStore(file.getResource());
        return ResultVO.ok();
    }

    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> download(@PathVariable("chatId") String chatId) {
        Resource resource = fileRepository.get(chatId);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        NameWithByteArrayResource nameResource = (NameWithByteArrayResource) resource;
        String encodeFileName = URLEncoder.encode(Objects.requireNonNull(nameResource.getFilename()), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                ).header("Content-Disposition",
                        "attachment; filename=\"" + encodeFileName + "\"")
                .body(resource);
    }

    @GetMapping("/chat")
    public Flux<String> chat(String prompt, String chatId) {
        Resource resource = fileRepository.get(chatId);
        NameWithByteArrayResource nameResource = (NameWithByteArrayResource) resource;


        // 保存会话历史
        historyRepository.save("pdf", chatId);
        return pdfChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "file_name == '" + nameResource.getFilename() + "'"))
                .stream().content();
    }

    /**
     * 保存文件到向量数据库中
     *
     * @param resource 文件资源
     */
    public void writeToVectorStore(Resource resource) {
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(
                resource,  // PDF文件资源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> documentList = pagePdfDocumentReader.read();
        documentList.forEach(doc -> documentIdList.add(doc.getId()));

        vectorStore.add(documentList);
    }

}
