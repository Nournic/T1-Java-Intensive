# Этап 1: Сборка с помощью Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

# Этап 2: Сборка легковесного образа
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/client-processing-ms-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]