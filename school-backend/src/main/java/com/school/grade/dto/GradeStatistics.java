package com.school.grade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeStatistics {

    private Long courseId;

    private String courseName;

    private BigDecimal avgScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    private Integer totalCount;
}
