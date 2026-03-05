# Use an official Maven image to build the application
#FROM maven:3.8.6-eclipse-temurin-17 as build
# Set the working directory in the container
#WORKDIR /app
# Copy the pom.xml and the source code
#COPY pom.xml /app/
#COPY src /app/src/
# Build the application
#RUN mvn clean package -DskipTests
# Use an official OpenJDK image as a base for the runtime
#FROM eclipse-temurin:17-jdk-alpine
# Set the working directory in the container

FROM maven:3.9.12-eclipse-temurin-17

WORKDIR /app

COPY ./target/Workflow-Service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
