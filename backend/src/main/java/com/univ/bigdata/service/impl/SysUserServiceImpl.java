package com.univ.bigdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.univ.bigdata.common.api.ResultCode;
import com.univ.bigdata.common.exception.CustomException;
import com.univ.bigdata.dto.LoginDto;
import com.univ.bigdata.dto.RegisterDto;
import com.univ.bigdata.entity.SysUser;
import com.univ.bigdata.mapper.SysUserMapper;
import com.univ.bigdata.security.JwtTokenUtil;
import com.univ.bigdata.service.SysUserService;
import com.univ.bigdata.vo.LoginVo;
import com.univ.bigdata.vo.UserInfoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginVo login(LoginDto loginDto) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDto.getUsername()));
        if (user == null) {
            throw new CustomException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomException(ResultCode.USER_PASSWORD_ERROR);
        }

        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole());

        return LoginVo.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public void register(RegisterDto registerDto) {
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDto.getUsername()));
        if (count > 0) {
            throw new CustomException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        SysUser newUser = SysUser.builder()
                .username(registerDto.getUsername())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .role("ROLE_USER")
                .createTime(LocalDateTime.now())
                .build();

        this.save(newUser);
    }

    @Override
    public UserInfoVo getUserInfo(String username) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new CustomException(ResultCode.USER_NOT_FOUND);
        }
        return UserInfoVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
    }
}
