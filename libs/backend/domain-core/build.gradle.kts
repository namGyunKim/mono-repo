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

tasks.named("classes") { dependsOn("generateContractEnumTs") }

tasks.register("generateContractEnumTs") {
    description = "contract enums → TypeScript 자동 생성"
    group = "codegen"

    val enumDir = file("src/main/java/com/example/domain/contract/enums")
    val outputFile = rootProject.file("libs/shared/types/src/api-contract-enums.ts")
    val enumPattern = Regex("""public\s+enum\s+(\w+)\s*\{""")
    val constantPattern = Regex("""^\s*([A-Z][A-Z0-9_]*)\s*[,(;]""")

    inputs.dir(enumDir)
    outputs.file(outputFile)

    doLast {
        val enumFiles = enumDir.listFiles()
            ?.filter { it.extension == "java" }
            ?.sortedBy { it.name }
            ?: error("enum 디렉토리를 찾을 수 없습니다: $enumDir")

        val sb = StringBuilder()
        sb.appendLine("/**")
        sb.appendLine(" * 이 파일은 자동 생성됩니다 — 직접 수정하지 마세요.")
        sb.appendLine(" * 생성 명령: ./gradlew :libs:backend:domain-core:generateContractEnumTs")
        sb.appendLine(" * 원본: libs/backend/domain-core/.../contract/enums/")
        sb.appendLine(" */")

        enumFiles.forEach { file ->
            val source = file.readText()
            val enumName = enumPattern.find(source)?.groupValues?.get(1) ?: return@forEach

            val constants = source.lines()
                .map { constantPattern.find(it)?.groupValues?.get(1) }
                .filterNotNull()
                .takeWhile { it != "NAME_MAP" }

            if (constants.isEmpty()) return@forEach

            sb.appendLine()
            sb.appendLine("export enum $enumName {")
            constants.forEach { name ->
                sb.appendLine("    $name = '$name',")
            }
            sb.appendLine("}")
        }
        sb.appendLine()

        outputFile.writeText(sb.toString())
        println("Generated ${enumFiles.size} enums → ${outputFile.relativeTo(rootProject.projectDir)}")
    }
}
