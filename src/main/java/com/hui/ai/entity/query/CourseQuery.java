package com.hui.ai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;


import java.util.List;

@Data
public class CourseQuery {
    // @ToolParam 注解是springAI用来解释Function参数的注解，其中的信息都会通过提示词的方式发送给大模型
    @ToolParam(required = false, description = "课程类型：编程、设计、自媒体、其它")
    private String type;
    @ToolParam(required = false, description = "学历要求：0-无、1-初中、2-高中、3-大专、4-本科及本科以上")
    private String edu;
    @ToolParam(required = false, description = "排序方式")
    private List<Sort> sorts;

    @Data
    public static class Sort{
        @ToolParam(required = false, description = "排序字段：price或duration")
        private String field;
        @ToolParam(required = false, description = "是否升序：true/false")
        private Boolean asc;
    }

}
