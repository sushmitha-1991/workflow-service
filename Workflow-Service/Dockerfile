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
# FROM openjdk:17-jdk-slim
# FROM maven:3.9.12-eclipse-temurin-17
FROM 10.247.129.8:8080/nic-java-17

WORKDIR /app

COPY ./target/Workflow-Service-1.0.0-SNAPSHOT.jar dsp2WorkflowLS.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "dsp2WorkflowLS.jar"]
