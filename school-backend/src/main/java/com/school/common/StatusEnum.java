package com.school.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 通用启用/禁用状态枚举
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final Integer code;
    private final String description;

    public boolean matches(Integer value) {
        return Objects.equals(code, value);
    }
}
