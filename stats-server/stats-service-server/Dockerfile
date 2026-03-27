FROM amazoncorretto:11-alpine-jdk

WORKDIR /app
COPY target/stats-service-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]