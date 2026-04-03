FROM eclipse-temurin:22-jre

WORKDIR /app
COPY src/main/java/org/example/app.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]