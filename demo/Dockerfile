# Dockerfile para deploy no Render
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar arquivos Maven primeiro (para cache de dependências)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permissão de execução ao Maven Wrapper
RUN chmod +x ./mvnw

# Baixar dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação (pular compilação e execução de testes)
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

# Verificar se o JAR foi criado
RUN ls -la /app/target/*.jar

# Imagem final
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR do build
COPY --from=build /app/target/todo-api-*.jar app.jar

# Expor porta
EXPOSE 8080

# Executar aplicação
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
