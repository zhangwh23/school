package com.school.grade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("grade")
public class Grade extends BaseEntity {

    private Long studentId;

    private Long courseId;

    private BigDecimal score;

    private String examType;

    private String semester;
}
