package com.NorthrnLights.demo.controller;

import com.NorthrnLights.demo.domain.Student;
import com.NorthrnLights.demo.domain.Teacher;
import com.NorthrnLights.demo.domain.User;
import com.NorthrnLights.demo.repository.StudentRepository;
import com.NorthrnLights.demo.repository.TeacherRepository;
import com.NorthrnLights.demo.service.StudentService;
import com.NorthrnLights.demo.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) MultipartFile profileImage,
            Authentication authentication) {
        
        try {
            // Verificar autenticação
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
            }
            
            Object principal = authentication.getPrincipal();
            User authenticatedUser = null;
            
            if (principal instanceof Student) {
                authenticatedUser = (Student) principal;
            } else if (principal instanceof Teacher) {
                authenticatedUser = (Teacher) principal;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tipo de usuário não reconhecido");
            }
            
            // Verificar se o usuário está tentando atualizar seu próprio perfil
            if (!authenticatedUser.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Você só pode atualizar seu próprio perfil");
            }
            
            User updatedUser = null;
            
            // Atualizar baseado no tipo de usuário
            if (authenticatedUser instanceof Student) {
                Student student = studentService.findById(id);
                if (userName != null && !userName.trim().isEmpty()) {
                    student.setUserName(userName);
                }
                if (email != null && !email.trim().isEmpty()) {
                    student.setEmail(email);
                }
                if (password != null && !password.trim().isEmpty()) {
                    student.setPassword(passwordEncoder.encode(password));
                }
                
                // Processar imagem de perfil
                if (profileImage != null && !profileImage.isEmpty()) {
                    String imagePath = saveProfileImage(profileImage, id, "student");
                    student.setProfileImage(imagePath);
                }
                
                // Atualizar campos básicos
                if (userName != null && !userName.trim().isEmpty()) {
                    student.setUserName(userName);
                }
                if (email != null && !email.trim().isEmpty()) {
                    student.setEmail(email);
                }
                if (password != null && !password.trim().isEmpty()) {
                    student.setPassword(passwordEncoder.encode(password));
                }
                
                // Processar imagem de perfil
                if (profileImage != null && !profileImage.isEmpty()) {
                    String imagePath = saveProfileImage(profileImage, id, "student");
                    student.setProfileImage(imagePath);
                }
                
                // Salvar usando repository diretamente
                updatedUser = studentRepository.save(student);
            } else if (authenticatedUser instanceof Teacher) {
                Teacher teacher = teacherService.findById(id);
                if (userName != null && !userName.trim().isEmpty()) {
                    teacher.setUserName(userName);
                }
                if (email != null && !email.trim().isEmpty()) {
                    teacher.setEmail(email);
                }
                if (password != null && !password.trim().isEmpty()) {
                    teacher.setPassword(passwordEncoder.encode(password));
                }
                
                // Processar imagem de perfil
                if (profileImage != null && !profileImage.isEmpty()) {
                    String imagePath = saveProfileImage(profileImage, id, "teacher");
                    teacher.setProfileImage(imagePath);
                }
                
                // Salvar usando repository diretamente
                updatedUser = teacherRepository.save(teacher);
            }
            
            log.info("✅ Perfil do usuário {} atualizado com sucesso", id);
            return ResponseEntity.ok(updatedUser);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Erro ao atualizar perfil do usuário {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erro ao atualizar perfil: " + e.getMessage());
        }
    }
    
    private String saveProfileImage(MultipartFile file, Long userId, String userType) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // Validar tipo de arquivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo deve ser uma imagem");
        }
        
        // Validar tamanho (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Imagem deve ter no máximo 5MB");
        }
        
        // Criar diretório se não existir
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profiles";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Gerar nome único para o arquivo
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = userType + "_" + userId + "_" + UUID.randomUUID().toString() + extension;
        
        // Salvar arquivo
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar caminho relativo
        String relativePath = "uploads/profiles/" + filename;
        log.info("✅ Imagem de perfil salva: {}", relativePath);
        return relativePath;
    }
}

