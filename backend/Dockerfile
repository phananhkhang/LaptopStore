FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

RUN apk add --no-cache maven

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -DskipTests


FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S spring && \
    adduser -S spring -G spring

COPY --from=builder --chown=spring:spring \
    /build/target/app.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]