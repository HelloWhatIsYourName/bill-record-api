FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_FILE=target/bill-record-api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
