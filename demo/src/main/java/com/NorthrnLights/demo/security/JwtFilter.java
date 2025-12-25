package com.NorthrnLights.demo.security;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.domain.User;
import com.NorthrnLights.demo.repository.StudentRepository;
import com.NorthrnLights.demo.repository.TeacherRepository;
import com.NorthrnLights.demo.repository.UserRepository;
import com.NorthrnLights.demo.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Permitir que requisições para /uploads passem sem autenticação
        String requestPath = request.getRequestURI();
        if (requestPath != null && requestPath.startsWith("/uploads/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ DEBUG: No Authorization header or not Bearer token for: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final String email = jwtService.extractUsername(token);

        System.out.println("🔐 DEBUG: Processing request to: " + request.getRequestURI());
        System.out.println("🔐 DEBUG: Token email: " + email);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && jwtService.validateToken(token)) {
                String roleName = user.getRole() != null ? user.getRole().name() : "USER";
                String authority = "ROLE_" + roleName;

                System.out.println("✅ DEBUG: Authenticating user: " + email + " with authority: " + authority);
                System.out.println("🔍 DEBUG: User role from database: " + user.getRole());
                System.out.println("🔍 DEBUG: Request URI: " + request.getRequestURI());
                System.out.println("🔍 DEBUG: Request Method: " + request.getMethod());

                // Buscar o objeto específico (Teacher ou Student) para usar como principal
                Object principal = user;
                if (user.getRole() == Role.TEACHER) {
                    Optional<Teacher> teacherOpt = teacherRepository.findByEmail(email);
                    if (teacherOpt.isPresent()) {
                        principal = teacherOpt.get();
                        System.out.println("✅ DEBUG: Teacher encontrado - ID: " + teacherOpt.get().getId());
                    } else {
                        System.out.println("⚠️ DEBUG: User tem role TEACHER mas não foi encontrado na tabela Teacher");
                        principal = user;
                    }
                } else if (user.getRole() == Role.STUDENT) {
                    Optional<Student> studentOpt = studentRepository.findByEmail(email);
                    if (studentOpt.isPresent()) {
                        principal = studentOpt.get();
                        System.out.println("✅ DEBUG: Student encontrado - ID: " + studentOpt.get().getId());
                    } else {
                        System.out.println("⚠️ DEBUG: User tem role STUDENT mas não foi encontrado na tabela Student");
                        principal = user;
                    }
                } else {
                    System.out.println("⚠️ DEBUG: Role desconhecido: " + user.getRole());
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal, // Usar Teacher/Student como principal, não o email
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(authority))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Debug após autenticação
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("🔍 DEBUG: Authentication set: " + (auth != null));
                if (auth != null) {
                    System.out.println("🔍 DEBUG: Principal type: " + auth.getPrincipal().getClass().getSimpleName());
                    System.out.println("🔍 DEBUG: Authorities: " + auth.getAuthorities());
                    System.out.println("🔍 DEBUG: Request URI: " + request.getRequestURI());
                    System.out.println("🔍 DEBUG: Request Method: " + request.getMethod());
                }
            } else {
                System.out.println("❌ DEBUG: User not found or token invalid");
            }
        } else {
            System.out.println("ℹ️ DEBUG: Already authenticated or no email");
        }

        filterChain.doFilter(request, response);
    }
}