package com.hui.ai.repository;

import java.util.List;

public interface ChatHistoryRepository {
    /**
     * 保存会话
     * @param type
     * @param chatId
     */
    void save(String type, String chatId);

    /**
     * 获取会话
     * @param type
     */
    List<String> get(String type);

    /**
     * 删除全部会话
     */
    void deleteAll();

    /**
     * 根据类型删除会话
     * @param type
     */
    void deleteByType(String type);

    /**
     * 根据类型和会话id删除会话
     * @param type
     * @param chatId
     */
    void delete(String type, String chatId);
}
