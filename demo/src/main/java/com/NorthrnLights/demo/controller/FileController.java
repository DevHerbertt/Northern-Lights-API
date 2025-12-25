package com.NorthrnLights.demo.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileController {

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serveFile(jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Obter o caminho completo da requisição
            String requestPath = request.getRequestURI();
            
            // Remover o contexto da aplicação se houver
            String contextPath = request.getContextPath();
            if (!contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
                requestPath = requestPath.substring(contextPath.length());
            }
            
            // Construir o caminho do arquivo
            // requestPath será algo como "/uploads/questions/arquivo.png"
            // Precisamos remover a barra inicial para ter "uploads/questions/arquivo.png"
            String filePath = requestPath.startsWith("/") 
                ? requestPath.substring(1) 
                : requestPath;
            
            // Decodificar a URL (converter %20 para espaços, etc)
            try {
                filePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                // Se falhar a decodificação, usar o caminho original
                System.out.println("⚠️ DEBUG FileController - Erro ao decodificar URL: " + e.getMessage());
            }
            
            // Usar caminho absoluto baseado no diretório do projeto
            String projectDir = System.getProperty("user.dir");
            Path file = Paths.get(projectDir, filePath);
            Resource resource = new FileSystemResource(file.toFile());
            
            // Se não encontrar, tentar caminhos alternativos
            if (!resource.exists()) {
                // Tentar com caminho absoluto
                java.io.File absoluteFile = file.toAbsolutePath().toFile();
                if (absoluteFile.exists()) {
                    resource = new FileSystemResource(absoluteFile);
                } else {
                    // Tentar sem decodificação (caso o arquivo tenha sido salvo com encoding diferente)
                    String originalPath = requestPath.startsWith("/") 
                        ? requestPath.substring(1) 
                        : requestPath;
                    java.io.File originalFile = new java.io.File(originalPath);
                    if (originalFile.exists()) {
                        resource = new FileSystemResource(originalFile);
                    } else {
                        // Log para debug
                        System.out.println("❌ DEBUG FileController - Arquivo não encontrado:");
                        System.out.println("   Request URI: " + request.getRequestURI());
                        System.out.println("   File Path (decoded): " + filePath);
                        System.out.println("   Absolute Path: " + file.toAbsolutePath());
                        
                        // Tentar listar o diretório para debug
                        Path parentDir = file.getParent();
                        if (parentDir != null && parentDir.toFile().exists()) {
                            System.out.println("   Parent directory exists: " + parentDir.toAbsolutePath());
                            java.io.File[] files = parentDir.toFile().listFiles();
                            if (files != null) {
                                System.out.println("   Files in directory:");
                                for (java.io.File f : files) {
                                    System.out.println("     - " + f.getName());
                                }
                            }
                        }
                    }
                }
            }

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determinar o tipo de conteúdo
            String contentType = determineContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerPath.endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }
}

