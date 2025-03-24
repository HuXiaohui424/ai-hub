package com.hui.ai.repository;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;



public interface FileRepository {

    /**
     * 保存文件
     * @param chatId   会话id
     * @param file 文件资源
     * @return 保存成功返回true，失败返回false
     */
     boolean save(String chatId, MultipartFile file) ;

    /**
     * 获取文件
     * @param chatId 会话id
     * @return 文件资源
     */
     Resource get(String chatId);
}
