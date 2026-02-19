# should be run from the root of the project!
FROM maven:3.9.12-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy all pom.xml files (Maven needs full project structure)
COPY pom.xml .
COPY core/pom.xml ./core/
COPY web-api/pom.xml ./web-api/
COPY desktop-client/pom.xml ./desktop-client/
COPY desktop-client/network/pom.xml ./desktop-client/network/
COPY desktop-client/client-common/pom.xml ./desktop-client/client-common/
COPY desktop-client/bg-client/pom.xml ./desktop-client/bg-client/
COPY desktop-client/gui-client/pom.xml ./desktop-client/gui-client/

RUN mvn dependency:go-offline -pl web-api -am -B

# Copy only sources needed for web-api
COPY core/src core/src
COPY web-api/src web-api/src

RUN mvn clean package -pl web-api -am -DskipTests -B

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/web-api/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]