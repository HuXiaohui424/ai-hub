package com.hui.ai.controller;

import com.hui.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "ai", produces = "text/html;charset=utf8")
public class ChatController {
    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    @PostMapping("/chat")
    public Flux<String> chat(String prompt, String chatId, @RequestParam(value = "files", required = false) List<MultipartFile> fileList) {
        // 保存会话id
        chatHistoryRepository.save("chat", chatId);

        if (fileList == null || fileList.isEmpty()) {
            return textChat(prompt, chatId);
        } else {
            return  multimodalChat(prompt,chatId, fileList);
        }
    }

    private Flux<String> multimodalChat(String prompt, String chatId, List<MultipartFile> fileList) {
        Media[] mediaArr = fileList.stream()
                .map(file -> new Media(
                        MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                        file.getResource()
                )).toArray(Media[]::new);

        return chatClient
                .prompt()
                .user(p -> p.text(prompt).media(mediaArr))
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream().content();

    }

    private Flux<String> textChat(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream().content();
    }


}
