package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.dto.StudentRegisterDTO;
import com.NorthrnLights.demo.repository.ExamGradeRepository;
import com.NorthrnLights.demo.repository.StudentRepository;
import com.NorthrnLights.demo.repository.WeeklyGradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ExamGradeRepository examGradeRepository;
    private final WeeklyGradeRepository weeklyGradeRepository;

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

    @Transactional
    public ResponseEntity<String> delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"
                ));

        log.info("🗑️ Iniciando exclusão do estudante ID: {} - Nome: {}", id, student.getUserName());

        // Deletar notas de provas relacionadas
        try {
            examGradeRepository.deleteByStudentId(id);
            log.info("✅ Notas de provas deletadas para o estudante ID: {}", id);
        } catch (Exception e) {
            log.warn("⚠️ Erro ao deletar notas de provas do estudante ID: {} - {}", id, e.getMessage());
        }

        // Deletar notas semanais relacionadas
        try {
            weeklyGradeRepository.deleteByStudentId(id);
            log.info("✅ Notas semanais deletadas para o estudante ID: {}", id);
        } catch (Exception e) {
            log.warn("⚠️ Erro ao deletar notas semanais do estudante ID: {} - {}", id, e.getMessage());
        }

        // As respostas (Answers) serão deletadas automaticamente devido ao cascade = CascadeType.ALL
        // no relacionamento @OneToMany na entidade Student

        // Deletar o estudante
        studentRepository.delete(student);
        log.info("✅ Estudante ID: {} deletado com sucesso", id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
