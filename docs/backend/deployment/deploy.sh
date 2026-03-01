#!/bin/bash
# =============================================================
#  Blue/Green 무중단 배포 스크립트
#  Usage: ./deploy.sh <project> [image-tag]
#  Example: ./deploy.sh user-api latest
#           ./deploy.sh user-api abc1234
#
#  서버의 /opt/deploy/deploy.sh 에 복사하여 사용한다.
#  프로젝트별 설정은 /opt/deploy/projects/<project>.env 에 정의한다.
# =============================================================
set -euo pipefail

# ----- 인자 파싱 -----
PROJECT="${1:?Usage: $0 <project> [image-tag]}"
TAG="${2:-latest}"
ENV_FILE="/opt/deploy/projects/${PROJECT}.env"

if [ ! -f "$ENV_FILE" ]; then
    echo "[ERROR] 환경 파일 없음: $ENV_FILE"
    exit 1
fi

source "$ENV_FILE"

FULL_IMAGE="${IMAGE}:${TAG}"

echo "======================================"
echo " Project : $PROJECT"
echo " Image   : $FULL_IMAGE"
echo "======================================"

# ----- 현재 활성 컬러 판별 -----
ACTIVE_FILE="/opt/deploy/projects/${PROJECT}.active"

if [ -f "$ACTIVE_FILE" ]; then
    CURRENT_COLOR=$(cat "$ACTIVE_FILE")
else
    # 최초 배포: 포트 응답 기반 판별
    if curl -sf "http://localhost:${BLUE_PORT}${HEALTH_PATH}" > /dev/null 2>&1; then
        CURRENT_COLOR="blue"
    elif curl -sf "http://localhost:${GREEN_PORT}${HEALTH_PATH}" > /dev/null 2>&1; then
        CURRENT_COLOR="green"
    else
        CURRENT_COLOR="none"
    fi
fi

if [ "$CURRENT_COLOR" = "blue" ]; then
    TARGET_COLOR="green"; TARGET_PORT=$GREEN_PORT
elif [ "$CURRENT_COLOR" = "green" ]; then
    TARGET_COLOR="blue"; TARGET_PORT=$BLUE_PORT
else
    TARGET_COLOR="blue"; TARGET_PORT=$BLUE_PORT
fi

echo "[INFO] 현재: ${CURRENT_COLOR} → 배포 대상: ${TARGET_COLOR} (port ${TARGET_PORT})"

# ----- 이미지 Pull -----
echo "[INFO] 이미지 Pull: ${FULL_IMAGE}"
docker pull "$FULL_IMAGE"

# ----- 기존 타겟 컨테이너 정리 -----
docker rm -f "${PROJECT}-${TARGET_COLOR}" 2>/dev/null || true

# ----- 새 컨테이너 기동 -----
echo "[INFO] 컨테이너 기동: ${PROJECT}-${TARGET_COLOR} (port ${TARGET_PORT})"
docker run -d \
    --name "${PROJECT}-${TARGET_COLOR}" \
    --network host \
    --restart unless-stopped \
    -v /app/logs:/app/logs \
    -e SERVER_PORT="${TARGET_PORT}" \
    -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES}" \
    -e LOGGING_FILE_NAME="${LOG_FILE}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_FILE_NAME_PATTERN="${LOG_ROLLING_PATTERN}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY="${LOG_MAX_HISTORY}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE="${LOG_MAX_FILE_SIZE}" \
    -e LOGGING_LOGBACK_ROLLINGPOLICY_TOTAL_SIZE_CAP="${LOG_TOTAL_SIZE_CAP}" \
    "$FULL_IMAGE"

# ----- Health Check -----
echo "[INFO] Health check 대기 (최대 ${HEALTH_TIMEOUT}초)..."
ELAPSED=0
while [ $ELAPSED -lt "${HEALTH_TIMEOUT}" ]; do
    if curl -sf "http://localhost:${TARGET_PORT}${HEALTH_PATH}" > /dev/null 2>&1; then
        echo "[OK] Health check 통과 (${ELAPSED}초)"
        break
    fi
    sleep 2
    ELAPSED=$((ELAPSED + 2))
done

if [ $ELAPSED -ge "${HEALTH_TIMEOUT}" ]; then
    echo "[FAIL] Health check 실패. 새 컨테이너 제거."
    docker logs "${PROJECT}-${TARGET_COLOR}" --tail 50
    docker rm -f "${PROJECT}-${TARGET_COLOR}"
    exit 1
fi

# ----- Nginx Upstream 전환 -----
NGINX_CONF="/opt/deploy/nginx/conf.d/${PROJECT}-upstream.conf"
cat > "$NGINX_CONF" <<EOF
upstream ${PROJECT} {
    server 127.0.0.1:${TARGET_PORT};
}
EOF

sudo nginx -t
if [ $? -ne 0 ]; then
    echo "[FAIL] Nginx 설정 검증 실패. 롤백."
    docker rm -f "${PROJECT}-${TARGET_COLOR}"
    exit 1
fi

sudo nginx -s reload
echo "[OK] Nginx upstream 전환 완료 → port ${TARGET_PORT}"

# ----- 이전 컨테이너 종료 -----
if [ "$CURRENT_COLOR" != "none" ]; then
    OLD_CONTAINER="${PROJECT}-${CURRENT_COLOR}"
    echo "[INFO] 이전 컨테이너 종료: ${OLD_CONTAINER}"
    # Graceful shutdown 대기 (진행 중 요청 완료)
    docker stop -t 30 "$OLD_CONTAINER" 2>/dev/null || true
    docker rm -f "$OLD_CONTAINER" 2>/dev/null || true
fi

# ----- 상태 기록 -----
echo "$TARGET_COLOR" > "$ACTIVE_FILE"

echo "======================================"
echo " 배포 완료: ${PROJECT}-${TARGET_COLOR} (port ${TARGET_PORT})"
echo "======================================"
