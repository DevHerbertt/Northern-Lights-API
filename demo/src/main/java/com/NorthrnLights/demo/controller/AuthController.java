package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.dto.AuthLogin;
import com.NorthrnLights.demo.dto.AuthRegister;
import com.NorthrnLights.demo.dto.AuthResponse;
import com.NorthrnLights.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRegister registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest, Role.STUDENT));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthLogin loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
