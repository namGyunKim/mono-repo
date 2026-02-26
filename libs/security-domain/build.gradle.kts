plugins {
    java
}

dependencies {
    implementation(project(":libs:global-core"))
    implementation(project(":libs:global-domain"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
