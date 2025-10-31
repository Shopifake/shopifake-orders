FROM eclipse-temurin:21-jdk AS base
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -DskipTests dependency:go-offline

FROM base AS dev
ENV PORT=8080
COPY src src
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-Dserver.port=${PORT} -Dspring.profiles.active=dev"]

FROM base AS build
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre AS prod
WORKDIR /app
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","-jar","/app/app.jar"]