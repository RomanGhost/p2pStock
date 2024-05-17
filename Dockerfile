# Stage 1: Build stage
FROM gradle:7.4.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Stage 2: Runtime stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
