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

val kotestVersion = "5.7.2"

dependencies {
    testImplementation("com.teststrategy.multimodule.maven:sf-hystrix")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Test framework
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    // Assertion library
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}