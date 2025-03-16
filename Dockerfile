FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/mcdockerseer.jar app.jar

ENTRYPOINT ["java","-Dcontainerised","-jar","app.jar"]
