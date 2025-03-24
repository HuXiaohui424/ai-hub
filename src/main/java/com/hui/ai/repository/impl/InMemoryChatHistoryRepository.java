package com.hui.ai.repository.impl;

import com.hui.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {
    private final ChatMemory chatMemory;
    private static final Map<String, List<String>> chatHistory = new HashMap<>();

    @Override
    public void save(String type, String chatId) {
        List<String> chatIdList = chatHistory.computeIfAbsent(type, k -> new ArrayList<>());
        if (!chatIdList.contains(chatId)) {
            // 没有当前会话id
            chatIdList.add(chatId);
        }
    }

    @Override
    public List<String> get(String type) {
        return chatHistory.getOrDefault(type, new ArrayList<>());
    }

    @Override
    public void deleteAll() {
        Set<String> keySet = chatHistory.keySet();
        for (String key : keySet) {
            deleteByType(key);
        }
        chatHistory.clear();
    }

    @Override
    public void deleteByType(String type) {
        List<String> chatIds = chatHistory.get(type);
        for (String chatId : chatIds) {
            chatMemory.clear(chatId);
        }
        chatHistory.remove(type);
    }

    @Override
    public void delete(String type, String chatId) {
        List<String> chatIdList = chatHistory.getOrDefault(type, List.of());
        chatIdList.remove(chatId);
        chatMemory.clear(chatId);
    }
}
