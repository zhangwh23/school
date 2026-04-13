package com.school.course.convert;

import com.school.course.dto.CourseDTO;
import com.school.course.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 课程对象转换器
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseConvert {

    CourseConvert INSTANCE = Mappers.getMapper(CourseConvert.class);

    Course convert(CourseDTO dto);

    void updateEntity(@MappingTarget Course entity, CourseDTO dto);
}
