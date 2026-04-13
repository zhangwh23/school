package com.school.teacher.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherDTO {

    @NotBlank(message = "教师编号不能为空")
    private String teacherNo;

    @NotBlank(message = "教师姓名不能为空")
    private String name;

    private Integer gender;

    private String phone;

    private String email;

    private String title;
}
