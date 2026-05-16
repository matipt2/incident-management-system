FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY backend/build.gradle backend/build.gradle
COPY backend/src backend/src

RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew && ./gradlew :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /workspace/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
