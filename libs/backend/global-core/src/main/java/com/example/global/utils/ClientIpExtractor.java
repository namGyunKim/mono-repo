package com.example.global.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 클라이언트 IP 추출 유틸
 * - 프록시/LB(예: AWS ALB), CDN(예: Cloudflare) 환경을 고려합니다.
 * - 인프라에서 헤더 스푸핑 방지를 위한 "신뢰 프록시" 설정이 별도로 필요할 수 있으나,
 * 베이스 프로젝트 특성상 여기서는 다양한 헤더를 순서대로 확인하는 방식으로 구현합니다.
 */
public final class ClientIpExtractor {

    private static final List<String> IP_HEADERS = List.of(
            "CF-Connecting-IP",     // Cloudflare
            "X-Real-IP",            // Nginx 등
            "X-Forwarded-For",      // 프록시 체인
            "Forwarded"             // RFC 7239
    );

    private ClientIpExtractor() {
    }

    /**
     * 가능한 경우 실제 클라이언트 IP를 반환합니다.
     * - 값이 없거나 파싱 실패 시 request.getRemoteAddr()로 폴백합니다.
     */
    public static String extract(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }

        try {
            for (final String header : IP_HEADERS) {
                final String raw = request.getHeader(header);
                final String candidate = parse(header, raw);
                if (hasText(candidate)) {
                    return candidate;
                }
            }

            final String remoteAddr = request.getRemoteAddr();
            return hasText(remoteAddr) ? remoteAddr.trim() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private static String parse(String headerName, String raw) {
        if (!hasText(raw)) {
            return null;
        }

        final String v = raw.trim();
        if (!hasText(v) || "unknown".equalsIgnoreCase(v)) {
            return null;
        }

        return switch (headerName) {
            case "X-Forwarded-For" -> normalizeIp(firstToken(v, ","));
            case "Forwarded" -> normalizeIp(parseForwardedFor(v));
            default -> normalizeIp(v);
        };
    }

    /**
     * Forwarded 헤더 예시:
     * - Forwarded: for=192.0.2.60;proto=http;by=203.0.113.43
     * - Forwarded: for="[2001:db8:cafe::17]:4711"
     */
    private static String parseForwardedFor(String forwarded) {
        if (!hasText(forwarded)) {
            return null;
        }

        final String first = firstToken(forwarded, ",");
        if (!hasText(first)) {
            return null;
        }

        final String[] parts = first.split(";");
        for (final String part : parts) {
            final String p = part.trim();
            if (p.isEmpty()) {
                continue;
            }

            if (p.regionMatches(true, 0, "for=", 0, 4)) {
                String value = p.substring(4).trim();
                value = stripQuotes(value);

                // IPv6: [....] 형태 처리
                if (value.startsWith("[")) {
                    final int end = value.indexOf(']');
                    if (end > 0) {
                        return value.substring(1, end);
                    }
                    return value.substring(1);
                }

                // IPv4:port 형태일 수 있음
                return value;
            }
        }

        return null;
    }

    private static String normalizeIp(String raw) {
        if (!hasText(raw)) {
            return null;
        }

        final String v = stripQuotes(raw.trim());
        if (!hasText(v) || "unknown".equalsIgnoreCase(v)) {
            return null;
        }

        // IPv6 bracket + port 형태: [2001:db8::1]:1234
        if (v.startsWith("[")) {
            final int end = v.indexOf(']');
            if (end > 0) {
                return v.substring(1, end);
            }
            return v.substring(1);
        }

        // IPv4:port 형태: 1.2.3.4:1234
        if (looksLikeIpv4WithPort(v)) {
            return v.substring(0, v.indexOf(':'));
        }

        // 그 외는 그대로 반환(IPv6 등)
        return v;
    }

    private static boolean looksLikeIpv4WithPort(String v) {
        if (!hasText(v)) {
            return false;
        }

        // ':'가 1개이고, '.'를 포함하면 IPv4:port로 간주
        final int firstColon = v.indexOf(':');
        if (firstColon < 0) {
            return false;
        }
        if (v.indexOf(':', firstColon + 1) >= 0) {
            return false;
        }
        return v.contains(".");
    }

    private static String firstToken(String raw, String delimiter) {
        if (!hasText(raw)) {
            return null;
        }

        final String[] tokens = raw.split(delimiter);
        if (tokens.length == 0) {
            return null;
        }

        final String first = tokens[0].trim();
        return hasText(first) ? first : null;
    }

    private static String stripQuotes(String v) {
        if (!hasText(v)) {
            return v;
        }

        final String s = v.trim();
        if (s.length() >= 2) {
            final char first = s.charAt(0);
            final char last = s.charAt(s.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return s.substring(1, s.length() - 1).trim();
            }
        }
        return s;
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
