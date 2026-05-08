FROM eclipse-temurin:21-jdk AS deps
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew dependencies --no-daemon || true

FROM deps AS build
WORKDIR /app

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app
USER app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]