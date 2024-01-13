FROM openjdk:20
ARG JAR_FILE=target/*.jar
COPY ./target/product-services-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "/app.jar"]