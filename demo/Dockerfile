# Dockerfile para deploy no Render
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar arquivos Maven primeiro (para cache de dependências)
COPY pom.xml .

# Baixar dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Verificar se o JAR foi criado
RUN ls -la /app/target/*.jar

# Imagem final
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR do build (usando nome específico)
COPY --from=build /app/target/todo-api-*.jar app.jar

# Expor porta
EXPOSE 8080

# Executar aplicação
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

