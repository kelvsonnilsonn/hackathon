# Estágio 1: Build com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte e faz o build
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Imagem final
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]