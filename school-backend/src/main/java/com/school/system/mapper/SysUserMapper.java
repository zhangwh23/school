package com.school.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
