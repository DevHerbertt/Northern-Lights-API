package com.NorthrnLights.demo.service;


import com.NorthrnLights.demo.domain.LevelEnglish;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.domain.User;
import com.NorthrnLights.demo.dto.StudentLoginDTO;
import com.NorthrnLights.demo.dto.StudentRegisterDTO;
import com.NorthrnLights.demo.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private StudentRegisterDTO registerDTO;
    private StudentLoginDTO loginDTO;

    @BeforeEach
    void setup() {
        registerDTO = StudentRegisterDTO.builder()
                .userName("John Doe")
                .email("john@example.com")
                .passWord("123456")
                .age(20)
                .levelEnglish(LevelEnglish.A2)
                .build();

        loginDTO = StudentLoginDTO.builder()
                .email("john@example.com")
                .passWord("123456")
                .build();
    }


    @Test
    void register_shouldSaveUser_whenValidData() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        studentService.register(registerDTO);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(userRepository).save(captor.capture());

        Student savedStudent = captor.getValue();
        assertThat(savedStudent.getEmail()).isEqualTo("john@example.com");
        assertThat(savedStudent.getPass_word()).isEqualTo("encodedPassword");
        assertThat(savedStudent.getUserName()).isEqualTo("John Doe");
    }


    @Test
    void register_shouldThrowException_whenPasswordIsNull() {
        registerDTO.setPassWord(null);

        assertThatThrownBy(() -> studentService.register(registerDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PassWord avoid");

        verify(userRepository, never()).save(any());
    }


    @Test
    void register_shouldThrowException_whenUserAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> studentService.register(registerDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User already exist");

        verify(userRepository, never()).save(any());
    }


    @Test
    void login_shouldReturnToken_whenCredentialsAreCorrect() {
        User user = Student.builder()
                .email("john@example.com")
                .pass_word("encodedPassword")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        String token = studentService.login(loginDTO);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }


    @Test
    void login_shouldThrowException_whenEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.login(loginDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email not found");
    }


    @Test
    void login_shouldThrowException_whenPasswordIncorrect() {
        User user = Student.builder()
                .email("john@example.com")
                .pass_word("encodedPassword")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> studentService.login(loginDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incorrect PassWord");
    }
}
