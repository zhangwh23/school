package com.school.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;
import com.school.system.vo.SysUserVO;

public interface SysUserService extends IService<SysUser> {

    Page<SysUserVO> pageUsers(Page<SysUser> page, String keyword);

    void createUser(SysUserDTO dto);

    void updateUser(Long id, SysUserDTO dto);

    void deleteUser(Long id);

    void resetPassword(Long id);
}