import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    id("org.springframework.boot") version "4.2.0-RC1" apply false
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.cloud.contract") version "5.0.3" apply false
}

allprojects {
    group = "ru.practicum"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    extra["springCloudVersion"] = "2026.0.0-RC1"
    // Spring Cloud Contract не входит в release train 2026.0, версия подключается отдельно
    extra["springCloudContractVersion"] = "5.0.3"
    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
            mavenBom("org.springframework.cloud:spring-cloud-contract-dependencies:${property("springCloudContractVersion")}")
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
        implementation("org.springframework.cloud:spring-cloud-starter-consul-config")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.boot:spring-boot-starter-security-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testCompileOnly("org.projectlombok:lombok:1.18.36")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
        implementation("org.mapstruct:mapstruct:1.6.3")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Фиксированное имя executable JAR для Dockerfile
    tasks.named<BootJar>("bootJar") {
        archiveFileName.set("${project.name}.jar")
    }
}
