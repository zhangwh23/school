package com.school.clazz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClazzVO {

    private Long id;

    private String className;

    private String gradeLevel;

    private Long teacherId;

    private String teacherName;

    private Integer studentCount;

    private Integer status;

    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}