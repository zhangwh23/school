package com.school.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long size;
    private long current;

    public PageResult(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }

    /**
     * 将 MyBatis-Plus 的 {@link IPage} 包装为统一分页响应
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }
}
