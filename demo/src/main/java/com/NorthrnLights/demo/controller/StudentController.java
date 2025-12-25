package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.dto.StudentRegisterDTO;
import com.NorthrnLights.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// StudentController.java - ATUALIZADO
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody StudentRegisterDTO studentDTO) {
        Student student = studentService.create(studentDTO);
        return ResponseEntity.ok(student);
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {

        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/quantity")
    public ResponseEntity<Integer> getStudentsQuantity() {
        return ResponseEntity.ok(studentService.findStudentsQuantity());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.findById(id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody StudentRegisterDTO studentDTO) {
        Student updatedStudent = studentService.update(id, studentDTO);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        return studentService.delete(id);
    }
}