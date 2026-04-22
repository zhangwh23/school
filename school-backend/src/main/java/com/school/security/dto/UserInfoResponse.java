package com.school.security.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private List<String> roles;
}
