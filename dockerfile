FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy your jar from exact location
COPY src/main/java/org/example/app.jar app.jar

ENV PORT=8080

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]