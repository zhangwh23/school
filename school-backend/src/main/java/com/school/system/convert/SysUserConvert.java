package com.school.system.convert;

import com.school.security.dto.UserInfoResponse;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 系统用户对象转换器
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SysUserConvert {

    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    /**
     * DTO → 实体（密码与状态由 service 自行决定，避免覆盖默认值）
     */
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    SysUser convert(SysUserDTO dto);

    /**
     * DTO → 已存在实体；密码不在更新流程中处理
     */
    @Mapping(target = "password", ignore = true)
    void updateEntity(@MappingTarget SysUser entity, SysUserDTO dto);

    /**
     * 实体 → 用户信息响应
     */
    UserInfoResponse toUserInfo(SysUser sysUser);
}
