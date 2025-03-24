package com.hui.ai;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.hui.ai.mapper")
@EnableFileStorage
public class HuiAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuiAiApplication.class, args);
    }

}
