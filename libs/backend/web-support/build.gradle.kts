plugins {
    java
}

dependencies {
    implementation(project(":libs:backend:global-core"))
    implementation(project(":libs:backend:global-domain"))
    implementation(project(":libs:backend:security-domain"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
