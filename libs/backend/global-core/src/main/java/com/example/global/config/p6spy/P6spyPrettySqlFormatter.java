package com.example.global.config.p6spy;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// P6spy 포맷 설정
public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {

    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss");

    // P6spy 포맷 메시지 설정
    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        final String formattedSql = formatSql(category, sql);
        final String logTime = LocalDateTime.now().format(LOG_TIME_FORMATTER);

        final String safeSql = formattedSql == null ? "" : formattedSql;
        return "%s | OperationTime : %dms%s".formatted(logTime, elapsed, safeSql);
    }

    // P6spy 포맷 메시지 설정 구현부
    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // Statement 범주만 포맷팅하며, DDL/DML을 구분합니다.
        if (Category.STATEMENT.getName().equals(category)) {
            final String normalizedSql = sql.trim().toLowerCase(Locale.ROOT);
            if (normalizedSql.startsWith("create") || normalizedSql.startsWith("alter") || normalizedSql.startsWith("comment")) {
                sql = FormatStyle.DDL.getFormatter().format(sql);
            } else {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
            }

            // [주의] 멀티라인 문자열은 text block을 사용합니다. (개행 하드코딩 금지)
            sql = """
                    |
                    HeFormatSql(P6Spy sql,Hibernate format):%s
                    """.formatted(sql);
        }

        return sql;
    }
}
