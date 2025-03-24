package com.hui.ai.controller;

import com.hui.ai.entity.vo.MessageVO;
import com.hui.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;

    @GetMapping("{type}")
    public List<String> getHistory(@PathVariable String type) {
        return chatHistoryRepository.get(type);
    }

    @GetMapping("{type}/{chatId}")
    public List<MessageVO> getHistoryMessage(@PathVariable String type, @PathVariable String chatId) {  // 形参type是为了和上面的方法区分开
        List<Message> historyMes = chatMemory.get(chatId, Integer.MAX_VALUE);
        return historyMes.stream().map(MessageVO::new).toList();
    }

}
