package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.domain.User;
import com.NorthrnLights.demo.dto.AuthLogin;
import com.NorthrnLights.demo.dto.AuthRegister;
import com.NorthrnLights.demo.dto.AuthResponse;
import com.NorthrnLights.demo.repository.UserRepository;

import com.NorthrnLights.demo.util.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRegister request, Role role) {
        if (role != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only students can register via this endpoint");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only students can register via this endpoint");
        }

        User user = Student.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUserName())
                .age(request.getAge())
                .levelEnglish(request.getLevelEnglish())
                .role(Role.STUDENT)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUserName(),
                user.getRole()
        );
    }

    public AuthResponse login(AuthLogin request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUserName(),
                user.getRole()
        );
    }
}