package com.school.clazz.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.clazz.entity.Clazz;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ClazzMapper extends BaseMapper<Clazz> {

    default List<Clazz> getListByIds(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapper<Clazz>()
                .in(Clazz::getId, ids));
    }
}
