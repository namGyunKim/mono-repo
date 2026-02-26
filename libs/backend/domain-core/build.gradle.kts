plugins {
    java
}

dependencies {
    implementation(project(":libs:backend:global-core"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
