package com.school.dashboard.dto;

import lombok.Data;

/**
 * 首页总览统计数据
 */
@Data
public class DashboardStatsVO {

    private Long studentCount;

    private Long teacherCount;

    private Long classCount;

    private Long courseCount;
}
