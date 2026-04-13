package com.school.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    INTERNAL_ERROR(500, "系统异常");

    private final int code;
    private final String message;
}
