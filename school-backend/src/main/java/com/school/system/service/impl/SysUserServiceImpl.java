package com.school.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.common.BusinessException;
import com.school.system.convert.SysUserConvert;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;
import com.school.system.entity.SysUserRole;
import com.school.system.mapper.SysUserMapper;
import com.school.system.mapper.SysUserRoleMapper;
import com.school.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * 系统用户 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<SysUser> pageUsers(Page<SysUser> page, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUserDTO dto) {
        SysUser user = SysUserConvert.INSTANCE.convert(dto);
        String rawPassword = StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD;
        user.setPassword(passwordEncoder.encode(rawPassword));
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        save(user);
        saveUserRoles(user.getId(), dto.getRoleIds());
        log.info("创建系统用户: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, SysUserDTO dto) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUserConvert.INSTANCE.updateEntity(user, dto);
        updateById(user);

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        saveUserRoles(id, dto.getRoleIds());
        log.info("更新系统用户: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        removeById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        log.info("删除系统用户: id={}", id);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        updateById(user);
        log.info("重置系统用户密码: id={}", id);
    }

    private void saveUserRoles(Long userId, Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<SysUserRole> records = roleIds.stream()
                .map(roleId -> {
                    SysUserRole record = new SysUserRole();
                    record.setUserId(userId);
                    record.setRoleId(roleId);
                    return record;
                })
                .toList();
        records.forEach(userRoleMapper::insert);
    }
}
