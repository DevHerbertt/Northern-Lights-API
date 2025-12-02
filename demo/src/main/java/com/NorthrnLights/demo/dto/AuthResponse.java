package com.NorthrnLights.demo.dto;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {
    private String token;
    private Long id;
    private String email;
    private String userName;
    private Role role;

}
