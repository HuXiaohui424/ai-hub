package com.hui.ai.controller;

import com.hui.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "ai", produces = "text/html;charset=utf8")
@RequiredArgsConstructor
public class CustomerServiceChatController {
    private final ChatClient serviceChatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    @GetMapping("service")
    public Flux<String> serviceChat(String prompt, String chatId) {
        // 保存会话
        chatHistoryRepository.save("service", chatId);
        // 调用大模型
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();

    }
}
