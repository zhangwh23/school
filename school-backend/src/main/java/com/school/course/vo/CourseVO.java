package com.school.course.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {

    private Long id;

    private String courseName;

    private String courseCode;

    private BigDecimal credit;

    private Long teacherId;

    private String teacherName;

    private Long classId;

    private String className;

    private String schedule;

    private Integer status;

    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}