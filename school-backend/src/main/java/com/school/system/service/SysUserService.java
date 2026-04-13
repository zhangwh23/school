package com.school.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;

/**
 * 系统用户 Service 接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    Page<SysUser> pageUsers(Page<SysUser> page, String keyword);

    /**
     * 创建用户
     *
     * @param dto 用户信息
     */
    void createUser(SysUserDTO dto);

    /**
     * 更新用户
     *
     * @param id  用户ID
     * @param dto 用户信息
     */
    void updateUser(Long id, SysUserDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 重置密码
     *
     * @param id 用户ID
     */
    void resetPassword(Long id);
}
