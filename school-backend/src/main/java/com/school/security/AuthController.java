package com.school.security;

import com.school.common.Result;
import com.school.security.dto.LoginRequest;
import com.school.security.dto.LoginResponse;
import com.school.security.dto.UserInfoResponse;
import com.school.system.convert.SysUserConvert;
import com.school.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证相关接口（登录、登出、当前用户信息）
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtUtils.generateToken(loginUser.getUsername());

        SysUser user = loginUser.getSysUser();
        log.info("用户登录成功: username={}", user.getUsername());
        return Result.success(new LoginResponse(token, user.getUsername(), user.getRealName(), loginUser.getRoles()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        UserInfoResponse info = SysUserConvert.INSTANCE.toUserInfo(loginUser.getSysUser());
        info.setRoles(loginUser.getRoles());
        return Result.success(info);
    }
}
