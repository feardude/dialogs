FROM eclipse-temurin:23-jre-alpine
LABEL maintainer=feardude
WORKDIR /app
COPY build/libs/dialogs-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8081
