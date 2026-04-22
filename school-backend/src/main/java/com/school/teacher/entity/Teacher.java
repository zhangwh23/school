package com.school.teacher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.school.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teacher")
public class Teacher extends BaseEntity {

    private Long userId;

    private String teacherNo;

    private String name;

    private Integer gender;

    private String phone;

    private String email;

    private String title;

    private Integer status = 1;
}
