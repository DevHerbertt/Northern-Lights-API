package com.NorthrnLights.demo.util;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.domain.User;
import com.NorthrnLights.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StartupRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String teacherEmail = "teacher@school.com";
        if (userRepository.findByEmail(teacherEmail).isEmpty()) {
            Teacher teacher = Teacher.builder()
                    .email(teacherEmail)
                    .password(passwordEncoder.encode("senhaForte123"))
                    .userName("TEACHER DEFAULT")
                    .age(27)
                    .role(Role.TEACHER)
                    .build();
            userRepository.save(teacher);
            System.out.println("Teacher default criado com sucesso.");
        }
    }
}
