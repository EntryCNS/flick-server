import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("org.springframework.security:spring-security-core")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // JWT
    api("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Utility
    implementation("org.apache.commons:commons-lang3:3.13.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:1.13.8")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.1")
    }
}

tasks.withType<BootJar> {
    enabled = false
}

tasks.withType<Jar> {
    enabled = true
}