plugins {
    java
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    repositories {
        mavenCentral()
    }

    configurations.named("compileOnly") {
        extendsFrom(configurations.getByName("annotationProcessor"))
    }

    dependencies {
        "implementation"(platform("software.amazon.awssdk:bom:2.41.23"))

        "implementation"("org.springframework.boot:spring-boot-starter-webmvc")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("org.springframework.boot:spring-boot-starter-aspectj")
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.springframework.boot:spring-boot-starter-security")
        "runtimeOnly"("org.postgresql:postgresql")

        "implementation"("io.github.openfeign.querydsl:querydsl-jpa:7.1")
        "annotationProcessor"("io.github.openfeign.querydsl:querydsl-apt:7.1:jakarta")
        "annotationProcessor"("jakarta.annotation:jakarta.annotation-api")
        "annotationProcessor"("jakarta.persistence:jakarta.persistence-api")

        "implementation"("io.jsonwebtoken:jjwt-api:0.13.0")
        "runtimeOnly"("io.jsonwebtoken:jjwt-impl:0.13.0")
        "runtimeOnly"("io.jsonwebtoken:jjwt-gson:0.13.0")

        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")
        "developmentOnly"("org.springframework.boot:spring-boot-devtools")

        "implementation"("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
        "implementation"("com.github.gavlyukovskiy:p6spy-spring-boot-starter:2.0.0") {
            exclude(group = "org.springframework.boot", module = "spring-boot-dependencies")
        }
        "implementation"("software.amazon.awssdk:s3")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
