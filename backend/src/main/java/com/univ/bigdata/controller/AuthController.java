package com.univ.bigdata.controller;

import com.univ.bigdata.common.api.Result;
import com.univ.bigdata.dto.LoginDto;
import com.univ.bigdata.dto.RegisterDto;
import com.univ.bigdata.service.SysUserService;
import com.univ.bigdata.vo.LoginVo;
import com.univ.bigdata.vo.UserInfoVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDto loginDto) {
        LoginVo loginVo = sysUserService.login(loginDto);
        return Result.success("登录成功", loginVo);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDto registerDto) {
        sysUserService.register(registerDto);
        return Result.success("注册成功", null);
    }

    @GetMapping("/info")
    public Result<UserInfoVo> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserInfoVo userInfo = sysUserService.getUserInfo(username);
        return Result.success(userInfo);
    }
}
