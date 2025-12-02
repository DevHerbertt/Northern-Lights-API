package com.NorthrnLights.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/test-public")
    public Map<String, String> testPublic() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Endpoint público funcionando");
        return response;
    }

    @GetMapping("/test-authenticated")
    public Map<String, Object> testAuthenticated(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Autenticado");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList()));
        return response;
    }

    @GetMapping("/test-teacher-annotation")
    @PreAuthorize("hasRole('TEACHER')")
    public Map<String, String> testTeacherAnnotation() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ @PreAuthorize hasRole('TEACHER') funcionando!");
        return response;
    }

    @GetMapping("/test-teacher-manual")
    public Map<String, Object> testTeacherManual(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"))) {
            response.put("message", "✅ Acesso concedido manualmente - ROLE_TEACHER encontrado");
            response.put("status", "success");
        } else {
            response.put("message", "❌ Acesso negado - ROLE_TEACHER não encontrado");
            response.put("authorities", authentication != null ? 
                authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList()) : "null");
            response.put("status", "error");
        }
        
        return response;
    }
}