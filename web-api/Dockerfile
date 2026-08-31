# should be run from the root of the project!
FROM maven:3.9.12-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy all pom.xml files (Maven needs full project structure)
COPY pom.xml .
COPY web-api/pom.xml ./web-api/
COPY desktop-client/pom.xml ./desktop-client/
COPY desktop-client/client-core/pom.xml ./desktop-client/client-core/
COPY desktop-client/client-app/pom.xml ./desktop-client/client-app/
COPY desktop-client/cli-client/pom.xml ./desktop-client/cli-client/
COPY desktop-client/gui-client/pom.xml ./desktop-client/gui-client/

RUN mvn dependency:go-offline -pl web-api -am -B

COPY web-api/src web-api/src

RUN mvn clean package -pl web-api -am -DskipTests -B

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/web-api/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]