package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.config.JwtUtil;
import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.model.User;
import com.exam.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, request.getUsername()));
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
        return new LoginResponse(token, user.getUsername(), user.getRealName(), user.getRole(), user.getId());
    }
    
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
    
    @Transactional
    public boolean createUser(User user) {
        if (getUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPlainPassword()));
        user.setStatus(1);
        return userMapper.insert(user) > 0;
    }
}
