FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY ./core ./core
COPY ./backend ./backend
COPY ./pom.xml .

RUN mvn -am clean package -DskipTests -Pweb

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]