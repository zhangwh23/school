package com.school.dashboard.service;

import com.school.dashboard.dto.DashboardStatsVO;

/**
 * 首页总览服务
 */
public interface DashboardService {

    /**
     * 获取首页总览统计数据
     *
     * @return 学生、教师、班级、课程数量汇总
     */
    DashboardStatsVO getStats();
}
