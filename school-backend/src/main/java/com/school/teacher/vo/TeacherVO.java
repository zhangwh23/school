package com.school.teacher.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherVO {

    private Long id;

    private Long userId;

    private String teacherNo;

    private String name;

    private Integer gender;

    private String genderName;

    private String phone;

    private String email;

    private String title;

    private Integer status;

    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}