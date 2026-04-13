package com.school.security;

import com.school.common.Result;
import com.school.security.dto.LoginRequest;
import com.school.security.dto.LoginResponse;
import com.school.security.dto.UserInfoResponse;
import com.school.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
        LoginResponse response = new LoginResponse(
                token, user.getUsername(), user.getRealName(), loginUser.getRoles());
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        SysUser user = loginUser.getSysUser();

        UserInfoResponse info = new UserInfoResponse();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRealName(user.getRealName());
        info.setAvatar(user.getAvatar());
        info.setPhone(user.getPhone());
        info.setEmail(user.getEmail());
        info.setRoles(loginUser.getRoles());
        return Result.success(info);
    }
}
