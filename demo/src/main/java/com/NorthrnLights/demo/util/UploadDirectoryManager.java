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
     * Prioriza variável de ambiente UPLOAD_DIR se configurada como caminho absoluto.
     * Caso contrário, tenta usar /app/uploads (persistente no Render).
     * Usa /tmp/uploads apenas como último recurso (temporário).
     * 
     * @return Caminho absoluto do diretório base de uploads
     */
    public synchronized String getBaseUploadDir() {
        if (baseUploadDir != null) {
            return baseUploadDir;
        }

        // Verificar se UPLOAD_DIR está configurado como variável de ambiente
        String uploadDirEnv = System.getenv("UPLOAD_DIR");
        String primaryDir;
        
        if (uploadDirEnv != null && !uploadDirEnv.trim().isEmpty()) {
            File envDir = new File(uploadDirEnv);
            if (envDir.isAbsolute()) {
                // Se UPLOAD_DIR é um caminho absoluto, usar diretamente
                primaryDir = uploadDirEnv.trim();
                log.info("📁 Usando UPLOAD_DIR da variável de ambiente (absoluto): {}", primaryDir);
            } else {
                // Se UPLOAD_DIR é relativo, usar user.dir como base
                String userDir = System.getProperty("user.dir");
                primaryDir = userDir + File.separator + uploadDirEnv.trim();
                log.info("📁 Usando UPLOAD_DIR da variável de ambiente (relativo): {} -> {}", uploadDirEnv, primaryDir);
            }
        } else {
            // Se não há variável de ambiente, usar padrão: user.dir/uploads
            String userDir = System.getProperty("user.dir");
            primaryDir = userDir + File.separator + "uploads";
            log.info("📁 UPLOAD_DIR não configurado, usando padrão: {}", primaryDir);
        }

        log.info("📁 Tentando configurar diretório de upload em: {}", primaryDir);
        log.info("📁 user.dir = {}", userDir);

        // Tentar criar e usar o diretório primário (/app/uploads)
        try {
            Path primaryPath = Paths.get(primaryDir);
            
            // Verificar se o diretório pai existe e tem permissões
            Path parentPath = primaryPath.getParent();
            if (parentPath != null) {
                File parentFile = parentPath.toFile();
                log.info("📁 Diretório pai: {} - Existe: {} - Pode escrever: {}", 
                    parentPath.toAbsolutePath(), parentFile.exists(), parentFile.canWrite());
            }
            
            // Tentar criar o diretório se não existir
            if (!Files.exists(primaryPath)) {
                log.info("📁 Criando diretório de upload: {}", primaryPath.toAbsolutePath());
                try {
                    // Tentar primeiro com Files.createDirectories
                    Files.createDirectories(primaryPath);
                    log.info("✅ Diretório criado com Files.createDirectories!");
                } catch (Exception createEx) {
                    log.warn("⚠️ Files.createDirectories falhou, tentando com File.mkdirs: {}", createEx.getMessage());
                    // Tentar alternativa com File.mkdirs
                    File dirFile = primaryPath.toFile();
                    boolean created = dirFile.mkdirs();
                    if (!created && !dirFile.exists()) {
                        log.error("❌ Ambos os métodos falharam ao criar diretório");
                        throw new Exception("Não foi possível criar diretório: " + createEx.getMessage(), createEx);
                    }
                    log.info("✅ Diretório criado com File.mkdirs!");
                }
            } else {
                log.info("📁 Diretório já existe: {}", primaryPath.toAbsolutePath());
            }

            // Verificar se o diretório existe agora
            if (!Files.exists(primaryPath)) {
                throw new Exception("Diretório não foi criado");
            }

            // Testar escrita real criando um arquivo temporário
            File testFile = new File(primaryPath.toFile(), ".test_write_" + System.currentTimeMillis());
            try {
                boolean created = testFile.createNewFile();
                if (created) {
                    testFile.delete();
                    log.info("✅ Teste de escrita bem-sucedido!");
                } else {
                    throw new Exception("Não foi possível criar arquivo de teste");
                }
            } catch (Exception writeEx) {
                log.error("❌ Erro ao testar escrita: {}", writeEx.getMessage(), writeEx);
                throw new Exception("Sem permissão de escrita: " + writeEx.getMessage(), writeEx);
            }

            // Verificar permissões
            File dirFile = primaryPath.toFile();
            if (dirFile.canWrite()) {
                baseUploadDir = primaryDir;
                log.info("✅✅✅ Diretório de upload determinado: {} (PERSISTENTE)", baseUploadDir);
                return baseUploadDir;
            } else {
                throw new Exception("Diretório existe mas não tem permissões de escrita");
            }
        } catch (Exception e) {
            // Se falhar, tentar usar /tmp como fallback
            log.warn("⚠️ Não foi possível usar {}: {}", primaryDir, e.getMessage());
            log.warn("⚠️ Stack trace: ", e);
            
            try {
                Path fallbackPath = Paths.get(FALLBACK_DIR);
                log.info("📁 Tentando usar fallback: {}", fallbackPath.toAbsolutePath());
                
                if (!Files.exists(fallbackPath)) {
                    Files.createDirectories(fallbackPath);
                }
                
                // Testar escrita no fallback também
                File testFile = new File(fallbackPath.toFile(), ".test_write_" + System.currentTimeMillis());
                boolean created = testFile.createNewFile();
                if (created) {
                    testFile.delete();
                }
                
                if (Files.exists(fallbackPath) && Files.isWritable(fallbackPath)) {
                    baseUploadDir = FALLBACK_DIR;
                    log.error("❌❌❌ USANDO /tmp/uploads COMO FALLBACK!");
                    log.error("❌❌❌ AVISO: Arquivos em /tmp serão PERDIDOS em reinicializações!");
                    log.error("❌❌❌ Configure permissões para /app/uploads para persistência!");
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

