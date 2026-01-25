# Dockerfile para deploy no Render
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar arquivos Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixar dependências (cache layer)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN ./mvnw clean package -DskipTests

# Imagem final
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR do build
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

