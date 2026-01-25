package com.NorthrnLights.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${DATABASE_USERNAME:}")
    private String databaseUsername;

    @Value("${DATABASE_PASSWORD:}")
    private String databasePassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Se DATABASE_URL estiver vazio, usar configuração padrão do application.yml
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            log.info("Usando configuração padrão do application.yml");
            return DataSourceBuilder.create().build();
        }

        // Se a URL começar com postgresql:// (formato do Render), converter para JDBC
        if (databaseUrl.startsWith("postgresql://") || databaseUrl.startsWith("postgres://")) {
            log.info("Detectado PostgreSQL - Convertendo URL do Render para formato JDBC");
            return createPostgreSQLDataSource();
        }

        // Se já estiver no formato jdbc:, usar diretamente
        if (databaseUrl.startsWith("jdbc:")) {
            log.info("URL já está no formato JDBC: {}", databaseUrl.substring(0, Math.min(50, databaseUrl.length())) + "...");
            return DataSourceBuilder.create()
                    .url(databaseUrl)
                    .username(databaseUsername)
                    .password(databasePassword)
                    .build();
        }

        // Fallback: usar configuração padrão
        log.warn("Formato de URL não reconhecido, usando configuração padrão");
        return DataSourceBuilder.create().build();
    }

    private DataSource createPostgreSQLDataSource() {
        try {
            // Parse da URL do Render: postgresql://user:pass@host:port/dbname
            URI dbUri = new URI(databaseUrl);

            String username = databaseUsername != null && !databaseUsername.isEmpty() 
                    ? databaseUsername 
                    : dbUri.getUserInfo().split(":")[0];
            
            String password = databasePassword != null && !databasePassword.isEmpty()
                    ? databasePassword
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
                    .username(username)
                    .password(password)
                    .build();

        } catch (URISyntaxException e) {
            log.error("Erro ao fazer parse da URL do banco de dados: {}", databaseUrl, e);
            throw new RuntimeException("Erro ao configurar banco de dados PostgreSQL", e);
        }
    }
}

