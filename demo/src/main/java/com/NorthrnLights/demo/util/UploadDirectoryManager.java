package com.NorthrnLights.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gerenciador centralizado para diretórios de upload.
 * Garante que os diretórios sejam criados corretamente e persistentes.
 */
@Slf4j
@Component
public class UploadDirectoryManager {

    private static String baseUploadDir = null;
    private static final String FALLBACK_DIR = "/tmp/uploads";

    /**
     * Obtém o diretório base para uploads.
     * Tenta usar /app/uploads primeiro (persistente no Render).
     * Usa /tmp/uploads apenas como último recurso (temporário).
     * 
     * @return Caminho absoluto do diretório base de uploads
     */
    public synchronized String getBaseUploadDir() {
        if (baseUploadDir != null) {
            return baseUploadDir;
        }

        String userDir = System.getProperty("user.dir");
        String primaryDir = userDir + File.separator + "uploads";

        // Tentar criar e usar o diretório primário (/app/uploads)
        try {
            Path primaryPath = Paths.get(primaryDir);
            
            // Tentar criar o diretório se não existir
            if (!Files.exists(primaryPath)) {
                log.info("📁 Tentando criar diretório de upload: {}", primaryPath.toAbsolutePath());
                Files.createDirectories(primaryPath);
            }

            // Verificar se podemos escrever
            if (Files.exists(primaryPath) && Files.isWritable(primaryPath)) {
                baseUploadDir = primaryDir;
                log.info("✅ Diretório de upload determinado: {} (PERSISTENTE)", baseUploadDir);
                return baseUploadDir;
            } else {
                throw new Exception("Diretório existe mas não tem permissões de escrita");
            }
        } catch (Exception e) {
            // Se falhar, tentar usar /tmp como fallback
            log.warn("⚠️ Não foi possível usar {}: {}. Tentando fallback...", primaryDir, e.getMessage());
            
            try {
                Path fallbackPath = Paths.get(FALLBACK_DIR);
                if (!Files.exists(fallbackPath)) {
                    Files.createDirectories(fallbackPath);
                }
                
                if (Files.exists(fallbackPath) && Files.isWritable(fallbackPath)) {
                    baseUploadDir = FALLBACK_DIR;
                    log.warn("⚠️⚠️⚠️ USANDO /tmp/uploads COMO FALLBACK!");
                    log.warn("⚠️⚠️⚠️ AVISO: Arquivos em /tmp serão PERDIDOS em reinicializações!");
                    log.warn("⚠️⚠️⚠️ Configure permissões para /app/uploads para persistência!");
                    return baseUploadDir;
                } else {
                    throw new Exception("Fallback também falhou");
                }
            } catch (Exception ex) {
                log.error("❌ Erro crítico: Não foi possível criar diretório de upload em nenhum local", ex);
                throw new RuntimeException("Não foi possível criar diretório de upload", ex);
            }
        }
    }

    /**
     * Obtém o diretório completo para um tipo específico de upload.
     * 
     * @param subDir Subdiretório (ex: "questions", "answers", "profiles")
     * @return Caminho absoluto do diretório
     */
    public String getUploadDir(String subDir) {
        String baseDir = getBaseUploadDir();
        String fullDir = baseDir + File.separator + subDir;
        
        // Garantir que o subdiretório existe
        try {
            Path subDirPath = Paths.get(fullDir);
            if (!Files.exists(subDirPath)) {
                Files.createDirectories(subDirPath);
                log.debug("✅ Subdiretório criado: {}", subDirPath.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("❌ Erro ao criar subdiretório {}: {}", subDir, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar subdiretório: " + subDir, e);
        }
        
        return fullDir;
    }

    /**
     * Verifica se o diretório base está usando o caminho persistente.
     * 
     * @return true se estiver usando /app/uploads, false se estiver usando /tmp
     */
    public boolean isUsingPersistentDirectory() {
        String dir = getBaseUploadDir();
        return !dir.equals(FALLBACK_DIR);
    }

    /**
     * Reseta o diretório base (útil para testes ou reconfiguração).
     */
    public synchronized void reset() {
        baseUploadDir = null;
    }
}

