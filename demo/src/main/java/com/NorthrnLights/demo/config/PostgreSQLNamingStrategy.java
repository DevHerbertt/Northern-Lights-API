package com.NorthrnLights.demo.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Estratégia de naming customizada para PostgreSQL que adiciona aspas
 * em identificadores que são palavras reservadas, especialmente "user".
 */
public class PostgreSQLNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        // Se o nome da tabela for "user" (palavra reservada no PostgreSQL), adicionar aspas
        if (name != null && "user".equalsIgnoreCase(name.getText())) {
            return Identifier.quote(name);
        }
        return super.toPhysicalTableName(name, context);
    }
}

