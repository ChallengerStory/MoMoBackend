FROM openjdk:17-jdk-slim
WORKDIR /app

# 수동 빌드한 JAR 경로 지정
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]