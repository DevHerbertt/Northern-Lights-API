package com.NorthrnLights.demo.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DatabaseMigrationRunner {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateTextColumns() {
        try {
            log.info("🔄 Iniciando migração de colunas de texto para TEXT...");
            
            // Verificar e alterar coluna 'text' na tabela 'answer'
            alterColumnIfNeeded("answer", "text", "TEXT");
            
            // Verificar e alterar coluna 'feedback' na tabela 'correction'
            alterColumnIfNeeded("correction", "feedback", "TEXT");
            
            // Verificar e alterar coluna 'description' na tabela 'question'
            alterColumnIfNeeded("question", "description", "TEXT");
            
            // Verificar e alterar coluna 'portuguese_translation' na tabela 'question'
            alterColumnIfNeeded("question", "portuguese_translation", "TEXT");
            
            log.info("✅ Migração de colunas de texto concluída com sucesso!");
            
        } catch (Exception e) {
            log.error("❌ Erro ao executar migração de colunas de texto: {}", e.getMessage(), e);
            // Não lançar exceção para não impedir a inicialização da aplicação
        }
    }

    private void alterColumnIfNeeded(String tableName, String columnName, String newType) {
        try {
            // Verificar o tipo atual da coluna
            String currentType = getColumnType(tableName, columnName);
            
            if (currentType != null) {
                // Normalizar o tipo atual para comparação
                String normalizedCurrentType = currentType.toUpperCase();
                
                // Verificar se já é TEXT ou LONGTEXT
                if (normalizedCurrentType.contains("TEXT")) {
                    log.info("✅ Coluna {}.{} já é do tipo TEXT (atual: {})", tableName, columnName, currentType);
                    return;
                }
                
                // Verificar se é VARCHAR e tem tamanho limitado
                if (normalizedCurrentType.contains("VARCHAR")) {
                    log.info("🔄 Alterando coluna {}.{} de {} para {}", tableName, columnName, currentType, newType);
                    String sql = String.format("ALTER TABLE %s MODIFY COLUMN %s %s", tableName, columnName, newType);
                    jdbcTemplate.execute(sql);
                    log.info("✅ Coluna {}.{} alterada com sucesso para {}", tableName, columnName, newType);
                } else {
                    log.info("⚠️ Coluna {}.{} tem tipo inesperado: {}. Tentando alterar para {}", 
                            tableName, columnName, currentType, newType);
                    String sql = String.format("ALTER TABLE %s MODIFY COLUMN %s %s", tableName, columnName, newType);
                    jdbcTemplate.execute(sql);
                    log.info("✅ Coluna {}.{} alterada com sucesso para {}", tableName, columnName, newType);
                }
            } else {
                log.warn("⚠️ Coluna {}.{} não encontrada. Pode ser criada automaticamente pelo Hibernate.", 
                        tableName, columnName);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao alterar coluna {}.{}: {}", tableName, columnName, e.getMessage());
            // Continuar com outras colunas mesmo se uma falhar
        }
    }

    private String getColumnType(String tableName, String columnName) {
        try {
            return jdbcTemplate.execute((Connection connection) -> {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
                
                if (columns.next()) {
                    String typeName = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    
                    // Retornar tipo completo (ex: VARCHAR(255))
                    if (typeName.equalsIgnoreCase("VARCHAR") || typeName.equalsIgnoreCase("CHAR")) {
                        return typeName + "(" + columnSize + ")";
                    }
                    return typeName;
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Erro ao verificar tipo da coluna {}.{}: {}", tableName, columnName, e.getMessage());
            return null;
        }
    }
}







