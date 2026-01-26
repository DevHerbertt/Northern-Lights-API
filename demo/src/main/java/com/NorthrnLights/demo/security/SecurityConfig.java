package com.NorthrnLights.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Filter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 DEBUG: Configurando SecurityFilterChain...");

        http
                .csrf(csrf -> {
                    System.out.println("🔧 DEBUG: CSRF desabilitado");
                    csrf.disable();
                })
                .cors(cors -> {
                    System.out.println("🔧 DEBUG: CORS configurado");
                    cors.configurationSource(corsConfigurationSource());
                })
                .sessionManagement(session -> {
                    System.out.println("🔧 DEBUG: Session STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(auth -> {
                    System.out.println("🔧 DEBUG: Configurando autorizações HTTP");
                    auth
                            // Permitir endpoints públicos
                            .requestMatchers(
                                    "/auth/**",
                                    "/api/auth/**",
                                    "/login",
                                    "/register",
                                    "/debug/**"
                            ).permitAll()

                            // Endpoints de questões:
                            // - Professores continuam precisando de permissão para criar/editar/apagar
                            // - Qualquer usuário (até não autenticado) pode visualizar lista/quantidade
                            .requestMatchers(HttpMethod.POST, "/questions/create").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.POST, "/questions/batch").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.POST, "/questions/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.PUT, "/questions/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.DELETE, "/questions/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/questions/quantity").permitAll()
                            .requestMatchers(HttpMethod.GET, "/questions", "/questions/**").permitAll()

                            // Endpoints de provas - professores podem tudo, estudantes podem visualizar
                            .requestMatchers(HttpMethod.POST, "/exams/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.PUT, "/exams/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.DELETE, "/exams/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/exams/**").hasAnyRole("TEACHER", "STUDENT")

                            // Endpoints de respostas - estudantes podem criar, professores podem ver
                            // Regra específica para POST /answers (sem wildcard) primeiro
                            .requestMatchers(HttpMethod.POST, "/answers").hasAnyRole("TEACHER", "STUDENT")
                            .requestMatchers(HttpMethod.POST, "/answers/**").hasAnyRole("TEACHER", "STUDENT")
                            .requestMatchers(HttpMethod.PUT, "/answers").hasAnyRole("TEACHER", "STUDENT")
                            .requestMatchers(HttpMethod.PUT, "/answers/**").hasAnyRole("TEACHER", "STUDENT")
                            .requestMatchers(HttpMethod.DELETE, "/answers/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/answers/my-answers").hasAnyRole("STUDENT")
                            .requestMatchers(HttpMethod.GET, "/answers").hasAnyRole("TEACHER", "STUDENT")
                            .requestMatchers(HttpMethod.GET, "/answers/**").hasAnyRole("TEACHER", "STUDENT")

                            // Endpoints de correções - professores podem criar/editar, estudantes podem visualizar
                            .requestMatchers(HttpMethod.POST, "/corrections").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.POST, "/corrections/**").hasAnyRole("TEACHER")
                            
                            // Endpoints de notas semanais - professores podem criar, estudantes podem visualizar
                            .requestMatchers(HttpMethod.POST, "/weekly-grades").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/weekly-grades/student/**").hasAnyRole("TEACHER", "STUDENT")
                            
                            // Endpoints de notas de provas - professores podem criar, estudantes podem visualizar
                            .requestMatchers(HttpMethod.POST, "/exam-grades").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/exam-grades/**").hasAnyRole("TEACHER", "STUDENT")
                            
                            // Endpoints de importação/exportação CSV - apenas professores
                            .requestMatchers("/csv/**").hasAnyRole("TEACHER")
                            
                            // Endpoints de aulas gravadas - professores podem criar/editar, estudantes podem visualizar
                            .requestMatchers(HttpMethod.POST, "/recorded-classes").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.PUT, "/recorded-classes/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.DELETE, "/recorded-classes/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/recorded-classes/**").hasAnyRole("TEACHER", "STUDENT")
                            
                            // Endpoints de usuários - usuários podem atualizar seu próprio perfil
                            .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("TEACHER", "STUDENT")
                            .requestMatchers(HttpMethod.PUT, "/corrections/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.DELETE, "/corrections/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/corrections/**").hasAnyRole("TEACHER", "STUDENT")

                            // Endpoints de meets - professores podem tudo, estudantes podem visualizar
                            .requestMatchers(HttpMethod.POST, "/meets").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.POST, "/meets/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.PUT, "/meets/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.DELETE, "/meets/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/meets", "/meets/**").hasAnyRole("TEACHER", "STUDENT")

                            // Endpoints de estudantes - apenas professores podem gerenciar
                            .requestMatchers(HttpMethod.DELETE, "/students/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.POST, "/students/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.PUT, "/students/**").hasAnyRole("TEACHER")
                            .requestMatchers(HttpMethod.GET, "/students/**").hasAnyRole("TEACHER", "STUDENT")

                            // Endpoints de professores:
                            // - Professores precisam de ROLE_TEACHER para criar/alterar/apagar
                            // - Qualquer usuário autenticado pode listar/visualizar (para evitar 403 no dashboard)
                            .requestMatchers(HttpMethod.GET, "/teachers", "/teachers/**").authenticated()
                            .requestMatchers(HttpMethod.POST, "/teachers", "/teachers/**").hasAuthority("ROLE_TEACHER")
                            .requestMatchers(HttpMethod.PUT, "/teachers/**").hasAuthority("ROLE_TEACHER")
                            .requestMatchers(HttpMethod.DELETE, "/teachers/**").hasAuthority("ROLE_TEACHER")

                            // Endpoints de email - apenas professores
                            .requestMatchers("/email/**").hasAnyRole("TEACHER")

                            // Permitir acesso a arquivos de upload (imagens, etc)
                            .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                            // Todas outras requisições precisam de autenticação
                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        System.out.println("✅ DEBUG: SecurityFilterChain configurado");
        return http.build();
    }

    // ... resto do código permanece igual
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("🔧 DEBUG: Configurando CORS...");

        CorsConfiguration configuration = new CorsConfiguration();

        // Obter URLs permitidas de variáveis de ambiente ou usar padrões
        String allowedOriginsEnv = System.getenv("ALLOWED_ORIGINS");
        
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            // Se houver variável de ambiente, usar ela (separada por vírgula)
            String[] origins = allowedOriginsEnv.split(",");
            configuration.setAllowedOrigins(Arrays.asList(origins));
            System.out.println("✅ DEBUG: CORS configurado com origens específicas: " + Arrays.toString(origins));
        } else {
            // Padrão: usar patterns para aceitar localhost + todos os domínios do Vercel
            // setAllowedOriginPatterns permite wildcards e funciona com allowCredentials
            configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",                              // Qualquer porta do localhost
                "https://*.vercel.app",                            // Todos os domínios do Vercel (preview e production)
                "https://northern-lights-frontend-2i36.vercel.app" // URL específica do frontend
            ));
            System.out.println("✅ DEBUG: CORS configurado com patterns (localhost + Vercel)");
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Permitir explicitamente o header Authorization (necessário para JWT)
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        System.out.println("✅ DEBUG: CORS configurado com sucesso");
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
