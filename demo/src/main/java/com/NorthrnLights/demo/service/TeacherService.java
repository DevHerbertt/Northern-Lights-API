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

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public Teacher create(TeacherDTO teacherDTO) {

        teacherDTO.setPassWord(passwordEncoder.encode(teacherDTO.getPassWord()));

        Teacher teacher = new Teacher();
        teacher.setUserName(teacherDTO.getUserName());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setAge(teacherDTO.getAge());
        teacher.setClassRoom(teacherDTO.getClassRoom());
        teacher.setRole(Role.TEACHER);

        return teacherRepository.save(teacher);
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
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

        if (teacherDetails.getPassWord() != null && !teacherDetails.getPassWord().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(teacherDetails.getPassWord()));
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
