# Use official Java runtime
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the app
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/target/allocationapi-0.0.1-SNAPSHOT.jar"]
