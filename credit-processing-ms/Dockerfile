# Этап 1: Сборка с помощью Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY client-processing-ms/pom.xml ./client-processing-ms/
COPY credit-processing-ms/pom.xml ./credit-processing-ms/
COPY account-processing-ms/pom.xml ./account-processing-ms/

COPY . .

RUN mvn clean install -B -DskipTests

# Этап 2: Сборка легковесного образа
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/credit-processing-ms/target/credit-processing-ms-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]