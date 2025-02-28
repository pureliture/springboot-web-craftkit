plugins {
    java
    kotlin("jvm") version "2.0.20"
    id("io.spring.dependency-management") version "1.1.5"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.teststrategy.multimodule.maven:sf-dependencies:1.0.0-SNAPSHOT")
        mavenBom("com.teststrategy.multimodule.maven:sf-root:1.0.0-SNAPSHOT")
    }
}

dependencies {
    testImplementation("com.teststrategy.multimodule.maven:sf-hystrix")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}