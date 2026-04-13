package com.school.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;
import com.school.system.entity.SysUserRole;
import com.school.system.mapper.SysUserMapper;
import com.school.system.mapper.SysUserRoleMapper;
import com.school.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系统用户 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public Page<SysUser> pageUsers(Page<SysUser> page, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUserDTO dto) {
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(user);

        // 保存用户角色关联
        saveUserRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, SysUserDTO dto) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        this.updateById(user);

        // 先删除原有角色关联，再重新插入
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        saveUserRoles(id, dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        this.removeById(id);
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        this.updateById(user);
    }

    /**
     * 保存用户角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }
}
