package com.school.clazz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clazz")
public class Clazz extends BaseEntity {

    private String className;

    private String gradeLevel;

    private Long teacherId;

    private Integer studentCount = 0;

    private Integer status = 1;
}
