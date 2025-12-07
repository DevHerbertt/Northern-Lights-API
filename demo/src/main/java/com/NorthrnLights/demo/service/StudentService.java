package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.dto.StudentRegisterDTO;
import com.NorthrnLights.demo.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public Student create(StudentRegisterDTO studentDTO) {
        Student student = new Student();
        student.setUserName(studentDTO.getUserName());
        student.setEmail(studentDTO.getEmail());
        student.setAge(studentDTO.getAge());
        student.setLevelEnglish(studentDTO.getLevelEnglish());
        student.setRole(Role.STUDENT);
        student.setLastLogin(null);                    // inicia sem login
        student.setAnswers(new ArrayList<>());
        student.setCreateAt(LocalDateTime.now());

        // Criptografa a senha antes de salvar
        student.setPassword(passwordEncoder.encode(studentDTO.getPassword()));

        return studentRepository.save(student);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }
    public int findStudentsQuantity() {
        return studentRepository.findAll().size();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id " + id));
    }

    public Student update(Long id, StudentRegisterDTO studentDetails) {
        Student student = findById(id);

        student.setUserName(studentDetails.getUserName());
        student.setEmail(studentDetails.getEmail());
        student.setAge(studentDetails.getAge());
        student.setLevelEnglish(studentDetails.getLevelEnglish());
        student.setRole(Role.STUDENT);

        if (studentDetails.getPassword() != null && !studentDetails.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
        }

        return studentRepository.save(student);
    }

    public ResponseEntity<String> delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"
                ));

        studentRepository.delete(student);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
