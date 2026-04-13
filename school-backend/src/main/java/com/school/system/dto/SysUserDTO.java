package com.school.system.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 系统用户 DTO
 */
@Data
public class SysUserDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态（1-正常，0-禁用）
     */
    private Integer status;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
