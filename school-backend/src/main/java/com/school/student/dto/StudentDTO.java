package com.school.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 学生数据传输对象 - 用于接收前端请求
 */
@Data
public class StudentDTO {

    /**
     * 更新时需要 id，创建时不需要
     */
    private Long id;

    private Long userId;

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer gender;

    private Integer age;

    private String phone;

    private String email;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    private LocalDate enrollmentDate;

    private Integer status = 1;
}