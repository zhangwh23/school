package com.school.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.Result;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;
import com.school.system.service.SysUserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户管理控制器
 */
@RestController
@RequestMapping("/api/system/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表
     *
     * @param page    页码（默认1）
     * @param size    每页数量（默认10）
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    @GetMapping
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> result = sysUserService.pageUsers(new Page<>(page, size), keyword);
        return Result.success(result);
    }

    /**
     * 新增用户
     *
     * @param dto 用户信息
     * @return 操作结果
     */
    @PostMapping
    public Result<?> create(@Valid @RequestBody SysUserDTO dto) {
        sysUserService.createUser(dto);
        return Result.success();
    }

    /**
     * 修改用户
     *
     * @param id  用户ID
     * @param dto 用户信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody SysUserDTO dto) {
        sysUserService.updateUser(id, dto);
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }
}
