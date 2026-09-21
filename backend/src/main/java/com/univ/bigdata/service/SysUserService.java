package com.univ.bigdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.univ.bigdata.dto.LoginDto;
import com.univ.bigdata.dto.RegisterDto;
import com.univ.bigdata.entity.SysUser;
import com.univ.bigdata.vo.LoginVo;
import com.univ.bigdata.vo.UserInfoVo;

public interface SysUserService extends IService<SysUser> {

    LoginVo login(LoginDto loginDto);

    void register(RegisterDto registerDto);

    UserInfoVo getUserInfo(String username);
}
