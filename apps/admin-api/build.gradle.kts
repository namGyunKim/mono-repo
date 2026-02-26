// 루트 build.gradle.kts에서 공통 설정 상속
dependencies {
    implementation(project(":libs:backend:global-core"))
    implementation(project(":libs:backend:domain-core"))
    implementation(project(":libs:backend:security-web"))
    implementation(project(":libs:backend:web-support"))
}
