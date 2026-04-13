package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    private String courseName;

    private String courseCode;

    private BigDecimal credit;

    private Long teacherId;

    private Long classId;

    private String schedule;

    private Integer status = 1;
}
