package com.school.student.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生视图对象 - 用于返回学生信息给前端
 */
@Data
public class StudentVO {

    private Long id;

    private Long userId;

    private String studentNo;

    private String name;

    /**
     * 性别（1=男 2=女）
     */
    private Integer gender;

    /**
     * 性别描述
     */
    private String genderName;

    private Integer age;

    private String phone;

    private String email;

    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollmentDate;

    /**
     * 状态（1=正常 0=禁用）
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}