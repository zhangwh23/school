package com.school.clazz.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClazzDTO {

    @NotBlank(message = "班级名称不能为空")
    private String className;

    private String gradeLevel;

    private Long teacherId;

    private Integer status;
}
