package com.school.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class StudentDTO {

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender;

    private Integer age;

    private String phone;

    private String email;

    private Long classId;

    private LocalDate enrollmentDate;
}
