dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("io.github.resilience4j:resilience4j-retry")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}
