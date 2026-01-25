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
        if (url == null || url.trim().isEmpty()) {
            url = properties.getUrl();
        }
        
        String username = System.getenv("DATABASE_USERNAME");
        if (username == null || username.trim().isEmpty()) {
            username = properties.getUsername();
        }
        
        String password = System.getenv("DATABASE_PASSWORD");
        if (password == null || password.trim().isEmpty()) {
            password = properties.getPassword();
        }

        // Se DATABASE_URL estiver vazia, usar configuração padrão do application.yml
        if (url == null || url.trim().isEmpty() || !url.contains("postgresql://") && !url.contains("postgres://")) {
            log.info("DATABASE_URL não encontrada ou não é PostgreSQL, usando configuração padrão do application.yml");
            // Usar configuração padrão do Spring Boot
            return DataSourceBuilder.create()
                    .url(properties.getUrl())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .driverClassName(properties.getDriverClassName())
                    .build();
        }

        log.info("DATABASE_URL detectada: {}", url.substring(0, Math.min(30, url.length())) + "...");
        
        // Se a URL começar com postgresql:// (formato do Render), converter para JDBC
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            log.info("Detectado PostgreSQL - Convertendo URL do Render para formato JDBC");
            return createPostgreSQLDataSource(url, username, password);
        }

        // Se já estiver no formato jdbc:postgresql, usar PostgreSQL
        if (url.startsWith("jdbc:postgresql://")) {
            log.info("Detectado PostgreSQL via JDBC URL");
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(url)
                    .username(username)
                    .password(password)
                    .build();
        }

        // Se já estiver no formato jdbc:mysql, usar MySQL
        if (url.startsWith("jdbc:mysql://")) {
            log.info("Detectado MySQL via JDBC URL");
            return DataSourceBuilder.create()
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .url(url)
                    .username(username)
                    .password(password)
                    .build();
        }

        // Fallback: tentar usar diretamente
        log.warn("Formato de URL não reconhecido, tentando usar diretamente: {}", 
                url.substring(0, Math.min(50, url.length())) + "...");
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
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

