FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S bookms && adduser -S bookms -G bookms
COPY --from=build /workspace/target/book-management-backend-*.jar /app/app.jar

USER bookms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
