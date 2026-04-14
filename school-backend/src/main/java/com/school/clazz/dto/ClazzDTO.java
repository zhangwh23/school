package com.school.clazz.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ClazzDTO {

    private Long id;

    @NotBlank(message = "班级名称不能为空")
    private String className;

    private String gradeLevel;

    @NotNull(message = "班主任ID不能为空")
    private Long teacherId;

    private Integer status = 1;
}