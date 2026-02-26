plugins {
    java
}

dependencies {
    implementation(project(":libs:backend:global-core"))
    implementation(project(":libs:backend:domain-core"))
    implementation(project(":libs:backend:security-web"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
