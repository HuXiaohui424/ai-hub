package com.hui.ai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.hui.ai.entity.po.Course;
import com.hui.ai.entity.po.CourseReservation;
import com.hui.ai.entity.po.School;
import com.hui.ai.entity.query.CourseQuery;
import com.hui.ai.service.ICourseReservationService;
import com.hui.ai.service.ICourseService;
import com.hui.ai.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CourseTools {
    private  final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;

    // @Tool Function的功能描述，将来会作为提示词的一部分，大模型依据这里的描述判断何时调用该函数
    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(
            @ToolParam(description = "查询的条件")
            CourseQuery query
    ) {

        // 查询条件为空
        if (query == null) {
            return courseService.list();
        }

        // 构建查询条件
        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType())
                .le(query.getEdu() != null, "edu", query.getEdu());

        // 动态添加排序条件
        if (query.getSorts() != null) {
            for (CourseQuery.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }

        // 返回查询结果
        return wrapper.list();
    }

    @Tool(description = "查询所有校区")
    public List<School> queryAllSchool() {
        return schoolService.list();
    }

    @Tool(description = "生成课程预约单,并返回生成的预约单号")
    public Integer generateCourseReservation( String courseName, String studentName, String contactInfo, String school, String remark) {
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(courseName);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setSchool(school);
        courseReservation.setRemark(remark);
        courseReservationService.save(courseReservation);

        return courseReservation.getId();
    }
}
