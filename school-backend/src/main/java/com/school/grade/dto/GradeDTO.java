package com.school.grade.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class GradeDTO {

    private Long id;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "成绩不能为空")
    private BigDecimal score;

    @NotBlank(message = "考试类型不能为空")
    private String examType;

    @NotBlank(message = "学期不能为空")
    private String semester;
}