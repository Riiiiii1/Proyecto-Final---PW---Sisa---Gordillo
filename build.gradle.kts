plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ec.edu.ups.icc"
version = "0.0.1-SNAPSHOT"
description = "ProyectoFinal_Sisa_Gordillo"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA / PostgreSQL
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // Seguridad
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT (jjwt)
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Validación (Bean Validation)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Rate limiting
    implementation("com.bucket4j:bucket4j_jdk17-core:8.19.0")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Reportes: PDF
    implementation("com.github.librepdf:openpdf:1.3.42")

    // Reportes: Excel
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // Mapstruct
    implementation("org.mapstruct:mapstruct:1.6.2")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Lombok + Mapstruct binding (necesario para que generen código juntos sin conflictos)
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}