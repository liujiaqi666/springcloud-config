package com.exam.controller;

import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.dto.Result;
import com.exam.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Result<Object> getUserInfo() {
        // 从 SecurityContext 获取用户信息
        return Result.success(null);
    }
}
