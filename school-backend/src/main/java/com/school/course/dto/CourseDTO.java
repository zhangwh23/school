package com.school.course.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class CourseDTO {

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    private BigDecimal credit;

    private Long teacherId;

    private Long classId;

    private String schedule;

    private Integer status;
}
