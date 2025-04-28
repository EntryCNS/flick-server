FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/service-registry.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]