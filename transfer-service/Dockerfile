# Executable JAR собирается заранее: ./gradlew bootJar
FROM eclipse-temurin:21-jre AS layers
WORKDIR /build
COPY build/libs/transfer-service.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre
RUN groupadd --system bank && useradd --system --gid bank bank
WORKDIR /application
COPY --from=layers /build/extracted/dependencies/ ./
COPY --from=layers /build/extracted/spring-boot-loader/ ./
COPY --from=layers /build/extracted/snapshot-dependencies/ ./
COPY --from=layers /build/extracted/application/ ./
USER bank
EXPOSE 8230
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
    CMD curl -fs http://localhost:8230/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
