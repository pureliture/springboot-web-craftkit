plugins {
    java
    id("io.spring.dependency-management") version "1.1.5"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.teststrategy.multimodule.maven:sample-extensions:1.0.0-SNAPSHOT")
        mavenBom("com.teststrategy.multimodule.maven:se-root:1.0.0-SNAPSHOT")
    }
}

dependencies {
    // Lombok (Maven에서 provided scope -> Gradle에서 compileOnly)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Logging & Utilities
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4")

    // ✅ 테스트 코드에서도 Lombok 사용 가능하도록 추가
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // kafka-inspector를 테스트에서만 사용하도록 설정
    testImplementation("com.teststrategy.multimodule.maven:kafka-inspector")

    // `se-test-common` 모듈 추가 (Maven 빌드 필요)
    testImplementation("com.teststrategy.multimodule.maven:se-test-common:1.0.0-SNAPSHOT")

    // Spring Cloud Stream Test Binder
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
}

tasks.withType<Test> {
    useJUnitPlatform()
}