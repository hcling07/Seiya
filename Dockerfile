FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/build/install/Seiya/ ./

ENV PORT=10000
EXPOSE 10000

CMD ["./bin/Seiya", "web"]
