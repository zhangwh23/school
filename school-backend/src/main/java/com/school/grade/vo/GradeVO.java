package com.school.grade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeVO {

    private Long id;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private Long courseId;

    private String courseName;

    private BigDecimal score;

    private String examType;

    private String semester;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}