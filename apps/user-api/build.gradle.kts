// 루트 build.gradle.kts에서 공통 설정 상속
dependencies {
    implementation(project(":libs:global-core"))
    implementation(project(":libs:global-domain"))
    implementation(project(":libs:security-domain"))
    implementation(project(":libs:web-support"))
}
