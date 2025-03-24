package com.hui.ai;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

@SpringBootTest
class HuiAiApplicationTests {

    @Autowired
    private  VectorStore vectorStore;

    @Test
    void contextLoads() {
        Resource resource = new FileSystemResource("中二知识笔记.pdf");
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                resource,  // PDF文件资源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );

        List<Document> documentList = pdfReader.read();
//        System.out.println(documentList);
//        System.out.println("--------------------------------------");
        vectorStore.add(documentList);
        SearchRequest searchRequest = SearchRequest.builder()
                .query("论语中教育的目的")
                .filterExpression("file_name == '中二知识笔记.pdf'")
                .topK(1)
                .similarityThreshold(0.6)
                .build();
//        List<Document> resultDocList = vectorStore.similaritySearch("论语中教育的目的");
        List<Document> resultDocList = vectorStore.similaritySearch(searchRequest);

        if (resultDocList == null || resultDocList.isEmpty()) {
            System.out.println("没有找到");
            return;
        }

        for (Document document : resultDocList) {
        System.out.println("####################################################");
            System.out.println(document.getId());
            System.out.println(document.getScore());
            System.out.println(document.getText());
            System.out.println(document.getFormattedContent());
        }

        SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
        simpleVectorStore.save(new File("chat-pdf.json"));


    }

}
