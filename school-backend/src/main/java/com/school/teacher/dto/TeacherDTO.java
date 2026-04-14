package com.school.teacher.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TeacherDTO {

    private Long id;

    private Long userId;

    @NotBlank(message = "教师编号不能为空")
    private String teacherNo;

    @NotBlank(message = "教师姓名不能为空")
    private String name;

    private Integer gender;

    private String phone;

    private String email;

    private String title;

    private Integer status = 1;
}