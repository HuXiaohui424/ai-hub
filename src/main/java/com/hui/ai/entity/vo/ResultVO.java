package com.hui.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVO {
    private Integer code;
    private String message;

    public static ResultVO ok() {
        return new ResultVO(1, "success");
    }

    public static ResultVO error(String msg) {
        return new ResultVO(0, msg);
    }
}
