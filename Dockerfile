FROM openjdk:18-ea-11-jdk-alpine3.13

ARG JAR_FILE=dichotomy-runner-app/target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]