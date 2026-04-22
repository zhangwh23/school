package com.school.system.convert;

import com.school.security.dto.UserInfoResponse;
import com.school.system.dto.SysUserDTO;
import com.school.system.entity.SysUser;
import com.school.system.vo.SysUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SysUserConvert {

    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    SysUser dtoToEntity(SysUserDTO dto);

    void updateEntityFromDto(@MappingTarget SysUser entity, SysUserDTO dto);

    UserInfoResponse entityToUserInfo(SysUser sysUser);

    SysUserVO entityToVo(SysUser entity);

    List<SysUserVO> entityToVoList(List<SysUser> list);
}