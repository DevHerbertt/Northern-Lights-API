package com.NorthrnLights.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        // Tentar ler da variável de ambiente primeiro
        String url = System.getenv("DATABASE_URL");
        String username = System.getenv("DATABASE_USERNAME");
        String password = System.getenv("DATABASE_PASSWORD");
        
        log.info("DATABASE_URL da variável de ambiente: {}", 
                url != null && url.length() > 30 ? url.substring(0, 30) + "..." : url);
        log.info("DATABASE_URL do properties: {}", 
                properties.getUrl() != null && properties.getUrl().length() > 30 
                    ? properties.getUrl().substring(0, 30) + "..." : properties.getUrl());
        
        // Se não houver DATABASE_URL da variável de ambiente, usar do properties
        if (url == null || url.trim().isEmpty()) {
            url = properties.getUrl();
            if (username == null || username.trim().isEmpty()) {
                username = properties.getUsername();
            }
            if (password == null || password.trim().isEmpty()) {
                password = properties.getPassword();
            }
            log.info("Usando configuração do application.yml (MySQL)");
        } else {
            log.info("DATABASE_URL encontrada nas variáveis de ambiente");
        }

        // Validar que temos uma URL válida
        if (url == null || url.trim().isEmpty()) {
            log.error("Nenhuma URL de banco de dados encontrada! Verifique DATABASE_URL ou spring.datasource.url");
            throw new IllegalStateException("URL do banco de dados não configurada. Configure DATABASE_URL ou spring.datasource.url");
        }

        // Se a URL é PostgreSQL (formato Render ou JDBC), processar
        if (url.startsWith("postgresql://") || url.startsWith("postgres://") || url.startsWith("jdbc:postgresql://")) {
            log.info("Detectado PostgreSQL - Processando URL");
            return processPostgreSQLUrl(url, username, password);
        }
        
        // Se é MySQL ou outra URL JDBC, usar diretamente
        if (url.startsWith("jdbc:")) {
            log.info("Detectado banco via JDBC URL: {}", url.startsWith("jdbc:mysql") ? "MySQL" : "Outro");
            return DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .driverClassName(properties.getDriverClassName())
                    .build();
        }
        
        // Se chegou aqui, é uma URL não reconhecida - usar properties como fallback
        log.warn("URL não reconhecida, usando configuração padrão do application.yml");
        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }
    
    private DataSource processPostgreSQLUrl(String url, String username, String password) {
        // Se já está no formato JDBC, usar diretamente
        if (url.startsWith("jdbc:postgresql://")) {
            log.info("URL já está no formato JDBC PostgreSQL");
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(url)
                    .username(username)
                    .password(password)
                    .build();
        }
        
        // Converter formato Render (postgresql://) para JDBC
        log.info("Convertendo URL do Render para formato JDBC");
        return createPostgreSQLDataSource(url, username, password);

    }

    private DataSource createPostgreSQLDataSource(String url, String username, String password) {
        try {
            // Parse da URL do Render: postgresql://user:pass@host:port/dbname
            URI dbUri = new URI(url);

            String finalUsername = (username != null && !username.isEmpty()) 
                    ? username 
                    : dbUri.getUserInfo().split(":")[0];
            
            String finalPassword = (password != null && !password.isEmpty())
                    ? password
                    : dbUri.getUserInfo().split(":")[1];

            String host = dbUri.getHost();
            int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
            String dbName = dbUri.getPath().replaceFirst("/", "");

            // Construir URL JDBC
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);

            log.info("Configurando PostgreSQL - Host: {}, Port: {}, Database: {}", host, port, dbName);

            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(finalUsername)
                    .password(finalPassword)
                    .build();

        } catch (URISyntaxException e) {
            log.error("Erro ao fazer parse da URL do banco de dados: {}", url, e);
            throw new RuntimeException("Erro ao configurar banco de dados PostgreSQL", e);
        } catch (Exception e) {
            log.error("Erro ao extrair credenciais da URL: {}", url, e);
            throw new RuntimeException("Erro ao configurar banco de dados PostgreSQL", e);
        }
    }
}

