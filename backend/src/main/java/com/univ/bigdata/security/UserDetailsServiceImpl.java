package com.univ.bigdata.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.bigdata.entity.SysUser;
import com.univ.bigdata.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(sysUser.getRole()))
        );
    }
}
