package com.NorthrnLights.demo.service;

import com.NorthrnLights.demo.domain.Role;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.dto.TeacherDTO;
import com.NorthrnLights.demo.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailForAcessTeacher;

    public Teacher create(TeacherDTO teacherDTO) {
        System.out.println("-----------------" + teacherDTO.getEmail());
        System.out.println("-----------------" + teacherDTO.getPassWord());

        // Verifique se a senha está vazia ou nula
        if (teacherDTO.getPassWord() == null || teacherDTO.getPassWord().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode estar vazia.");
        }

        // SALVAR a senha ORIGINAL antes de codificar
        String senhaOriginal = teacherDTO.getPassWord();

        // Codificar a senha
        teacherDTO.setPassWord(passwordEncoder.encode(teacherDTO.getPassWord()));

        Teacher teacher = new Teacher();
        teacher.setUserName(teacherDTO.getUserName());
        teacher.setPassword(teacherDTO.getPassWord());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setAge(teacherDTO.getAge());
        teacher.setClassRoom(teacherDTO.getClassRoom());
        teacher.setRole(Role.TEACHER);
        teacher.setCreateAt(LocalDateTime.now());

        // Restaurar a senha ORIGINAL no DTO antes de enviar o e-mail
        teacherDTO.setPassWord(senhaOriginal);

        // Enviar e-mail com a senha ORIGINAL
       emailForAcessTeacher.sendEmailCreat(teacherDTO);

        return teacherRepository.save(teacher);
    }


    public List<Teacher> findAll() {
        List<Teacher> all = teacherRepository.findAll();

        return all.stream()
                .filter(teacher -> teacher.getId() != 1) // filtra id 1
                .collect(Collectors.toList());
    }

    public int findAllQuantity() {
        return teacherRepository.findAll().size();
    }

    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id " + id));
    }

    public Teacher update(Long id, TeacherDTO teacherDetails) {
        if (!teacherRepository.existsById(id)) {
            log.warn("Teacher with ID {} not found ", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found");
        }
        Teacher teacher = findById(id);
        teacher.setUserName(teacherDetails.getUserName());
        teacher.setEmail(teacherDetails.getEmail());
        teacher.setAge(teacherDetails.getAge());
        teacher.setClassRoom(teacherDetails.getClassRoom());
        teacher.setRole(Role.TEACHER);
        teacher.setUpdateAt(LocalDateTime.now());

        if (teacherDetails.getPassWord() != null && !teacherDetails.getPassWord().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(teacherDetails.getPassWord()));
            emailForAcessTeacher.sendEmailCreat(teacherDetails);
        }

        return teacherRepository.save(teacher);
    }

    public ResponseEntity<String> delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            log.warn("Teacher with ID {} not found for deletion", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question not found");
        }
        Teacher teacher = findById(id);
        teacherRepository.delete(teacher);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
