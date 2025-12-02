package com.NorthrnLights.demo.security;

import com.NorthrnLights.demo.domain.User;
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

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ DEBUG: No Authorization header or not Bearer token");
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

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(authority))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Debug após autenticação
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("🔍 DEBUG: Authentication set: " + (auth != null));
                if (auth != null) {
                    System.out.println("🔍 DEBUG: Authorities: " + auth.getAuthorities());
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