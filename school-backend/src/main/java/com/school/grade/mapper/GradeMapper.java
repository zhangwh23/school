package com.school.grade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.grade.dto.GradeStatistics;
import com.school.grade.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

    @Select("SELECT g.course_id, c.course_name, AVG(g.score) as avg_score, MAX(g.score) as max_score, MIN(g.score) as min_score, COUNT(*) as total_count FROM grade g JOIN course c ON g.course_id = c.id WHERE g.semester = #{semester} GROUP BY g.course_id, c.course_name")
    List<GradeStatistics> selectStatistics(@Param("semester") String semester);
}
