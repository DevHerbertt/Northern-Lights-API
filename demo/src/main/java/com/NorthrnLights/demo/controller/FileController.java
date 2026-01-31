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
            
            // Tentar primeiro com user.dir (normalmente /app no Render)
            String userDir = System.getProperty("user.dir");
            java.io.File file1 = new java.io.File(userDir, filePath);
            Resource resource = null;
            
            if (file1.exists() && file1.isFile()) {
                resource = new FileSystemResource(file1);
            } else {
                // Tentar com /tmp como fallback
                java.io.File file2 = new java.io.File("/tmp", filePath);
                if (file2.exists() && file2.isFile()) {
                    resource = new FileSystemResource(file2);
                } else {
                    // Log para debug
                    System.out.println("❌ DEBUG FileController - Arquivo não encontrado:");
                    System.out.println("   Request URI: " + request.getRequestURI());
                    System.out.println("   File Path (decoded): " + filePath);
                    System.out.println("   Tentado em: " + file1.getAbsolutePath());
                    System.out.println("   Tentado em: " + file2.getAbsolutePath());
                    
                    // Tentar listar o diretório para debug
                    java.io.File parentDir1 = file1.getParentFile();
                    if (parentDir1 != null && parentDir1.exists()) {
                        System.out.println("   Parent directory 1 exists: " + parentDir1.getAbsolutePath());
                        java.io.File[] files = parentDir1.listFiles();
                        if (files != null) {
                            System.out.println("   Files in directory 1:");
                            for (java.io.File f : files) {
                                System.out.println("     - " + f.getName());
                            }
                        }
                    }
                    
                    java.io.File parentDir2 = file2.getParentFile();
                    if (parentDir2 != null && parentDir2.exists()) {
                        System.out.println("   Parent directory 2 exists: " + parentDir2.getAbsolutePath());
                        java.io.File[] files = parentDir2.listFiles();
                        if (files != null) {
                            System.out.println("   Files in directory 2:");
                            for (java.io.File f : files) {
                                System.out.println("     - " + f.getName());
                            }
                        }
                    }
                }
            }

            if (resource == null || !resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determinar o tipo de conteúdo
            String contentType = determineContentType(filePath);
            
            // Obter o nome do arquivo do resource
            String filename = resource.getFilename();
            if (filename == null) {
                // Extrair do caminho se não disponível
                filename = filePath.substring(filePath.lastIndexOf('/') + 1);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
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

