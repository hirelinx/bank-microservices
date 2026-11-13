import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

plugins {
    id("org.springframework.cloud.contract")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("io.github.resilience4j:resilience4j-retry")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
    testImplementation(project(path = ":notification-service", configuration = "contractStubs"))

    contractTestImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
}

contracts {
    testFramework.set(TestFramework.JUNIT5)
    testMode.set(TestMode.MOCKMVC)
    baseClassForTests.set("ru.practicum.account.contract.ContractVerifierBase")
}

// Stub-ы для consumer-тестов других модулей (Stub Runner в режиме classpath)
val contractStubs by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts {
    add(contractStubs.name, tasks.named("verifierStubsJar"))
}
