FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy jar from target folder
COPY target/*.jar app.jar

# Fly.io requires dynamic port
ENV PORT=8080

EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar"]