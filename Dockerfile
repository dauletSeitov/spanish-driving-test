FROM openjdk
WORKDIR /app
COPY build/libs/spanish-driving-test-1.0-SNAPSHOT.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]