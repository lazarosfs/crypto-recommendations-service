# Use a base image with Java
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the jar file from the local target directory
COPY target/CryptoRecommendationsService-0.0.1-SNAPSHOT.jar CryptoRecommendationsService.jar

# Expose the port on which the application runs
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "CryptoRecommendationsService.jar", "-web -webAllowOthers -tcp -tcpAllowOthers -browser"]

