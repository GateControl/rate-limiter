FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]