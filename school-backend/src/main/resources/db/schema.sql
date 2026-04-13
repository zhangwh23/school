-- 校园管理系统数据库初始化脚本
-- 数据库: school

CREATE DATABASE IF NOT EXISTS `school` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `school`;

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用 0禁用）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(50) NOT NULL COMMENT '角色标识',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 学生表
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID',
    `student_no` VARCHAR(30) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT DEFAULT NULL COMMENT '性别（1男 2女）',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
    `enrollment_date` DATE DEFAULT NULL COMMENT '入学日期',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 教师表
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID',
    `teacher_no` VARCHAR(30) NOT NULL COMMENT '工号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT DEFAULT NULL COMMENT '性别（1男 2女）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `title` VARCHAR(50) DEFAULT NULL COMMENT '职称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher_no` (`teacher_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 班级表
DROP TABLE IF EXISTS `clazz`;
CREATE TABLE `clazz` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `class_name` VARCHAR(50) NOT NULL COMMENT '班级名称',
    `grade_level` VARCHAR(20) DEFAULT NULL COMMENT '年级',
    `teacher_id` BIGINT DEFAULT NULL COMMENT '班主任ID',
    `student_count` INT NOT NULL DEFAULT 0 COMMENT '学生人数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 课程表
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `course_code` VARCHAR(30) NOT NULL COMMENT '课程编号',
    `credit` DECIMAL(3,1) DEFAULT NULL COMMENT '学分',
    `teacher_id` BIGINT DEFAULT NULL COMMENT '任课教师ID',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
    `schedule` VARCHAR(100) DEFAULT NULL COMMENT '上课时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 成绩表
DROP TABLE IF EXISTS `grade`;
CREATE TABLE `grade` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `score` DECIMAL(5,2) DEFAULT NULL COMMENT '成绩',
    `exam_type` VARCHAR(20) DEFAULT NULL COMMENT '考试类型（期中/期末/平时）',
    `semester` VARCHAR(20) DEFAULT NULL COMMENT '学期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- ========== 初始数据 ==========

-- 初始化角色
INSERT INTO `sys_role` (`role_name`, `role_key`) VALUES
('管理员', 'ADMIN'),
('教师', 'TEACHER'),
('学生', 'STUDENT');

-- 初始化管理员账号（密码: admin123，BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1);

-- 管理员角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 示例教师账号（密码: 123456）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`) VALUES
('teacher01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (2, 2);

INSERT INTO `teacher` (`user_id`, `teacher_no`, `name`, `gender`, `phone`, `title`) VALUES
(2, 'T20250001', '张老师', 1, '13800138001', '讲师');

-- 示例学生账号（密码: 123456）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`) VALUES
('student01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李同学', 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (3, 3);

-- 示例班级
INSERT INTO `clazz` (`class_name`, `grade_level`, `teacher_id`, `student_count`) VALUES
('计算机科学1班', '2025级', 1, 1);

-- 示例学生
INSERT INTO `student` (`user_id`, `student_no`, `name`, `gender`, `age`, `phone`, `class_id`, `enrollment_date`) VALUES
(3, 'S20250001', '李同学', 1, 20, '13800138002', 1, '2025-09-01');

-- 示例课程
INSERT INTO `course` (`course_name`, `course_code`, `credit`, `teacher_id`, `class_id`, `schedule`) VALUES
('Java程序设计', 'CS101', 4.0, 1, 1, '周一 1-2节'),
('数据结构', 'CS102', 3.5, 1, 1, '周三 3-4节');
