dependencies {
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
}
