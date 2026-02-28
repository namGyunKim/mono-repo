# 모든 Spring Boot 앱이 공유하는 단일 Dockerfile.
# CI에서 프로젝트별 JAR를 app.jar로 복사한 뒤 이 Dockerfile로 빌드한다.
#
# 레포지토리 위치: docs/deployment/backend.Dockerfile
# 실제 배치 위치: infra/docker/backend.Dockerfile (CI가 참조)

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
