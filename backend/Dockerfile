# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Environment variables
ENV DB_USERNAME=edna_user \
    DB_PASSWORD=edna_password \
    DB_HOST=localhost \
    DB_PORT=5432 \
    DB_NAME=edna_biodiversity \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
