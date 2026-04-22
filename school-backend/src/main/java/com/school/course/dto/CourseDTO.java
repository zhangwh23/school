package com.school.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CourseDTO {

    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    private BigDecimal credit;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    private String schedule;

    private Integer status = 1;
}