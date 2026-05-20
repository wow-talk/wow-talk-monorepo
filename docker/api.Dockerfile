FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /repo

COPY gradle ./gradle
COPY gradlew settings.gradle build.gradle ./
COPY apps/api/build.gradle ./apps/api/build.gradle
COPY backend/core/build.gradle ./backend/core/build.gradle
COPY backend/transport/build.gradle ./backend/transport/build.gradle
COPY backend/rawtcp/build.gradle ./backend/rawtcp/build.gradle
COPY backend/websocket/build.gradle ./backend/websocket/build.gradle

RUN ./gradlew :apps:api:dependencies --no-daemon

COPY apps/api/src ./apps/api/src
COPY backend/core/src ./backend/core/src
COPY backend/transport/src ./backend/transport/src
COPY backend/rawtcp/src ./backend/rawtcp/src
COPY backend/websocket/src ./backend/websocket/src

RUN ./gradlew :apps:api:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /repo/apps/api/build/libs/*.jar ./app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
