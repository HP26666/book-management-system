FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src
RUN for attempt in 1 2 3; do \
			mvn -q -DskipTests -Dmaven.wagon.http.retryHandler.count=5 package && break; \
			if [ "$attempt" -eq 3 ]; then exit 1; fi; \
		done

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S bookms && adduser -S bookms -G bookms
COPY --from=build /workspace/target/book-management-backend-*.jar /app/app.jar
RUN mkdir -p /app/uploads && chown -R bookms:bookms /app

USER bookms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
