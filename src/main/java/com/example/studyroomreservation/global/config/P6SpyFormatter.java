package com.example.studyroomreservation.global.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class P6SpyFormatter implements MessageFormattingStrategy {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("'[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+'");

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        String formattedSql = formatSql(category, sql);
        String maskedSql = maskSensitiveData(formattedSql);

        return String.format("[%s] | %d ms | %s", category, elapsed, maskedSql);
    }

    private String formatSql(String category, String sql) {
        if (!StringUtils.hasText(sql) || sql.trim().isEmpty()) {
            return "";
        }

        if ("statement".equals(category)) {
            String tmpsql = sql.trim().toLowerCase(Locale.ROOT);
            if (tmpsql.startsWith("create") || tmpsql.startsWith("alter") || tmpsql.startsWith("comment")) {
                sql = FormatStyle.DDL.getFormatter().format(sql);
            } else {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
            }
        }
        return sql;
    }

    private String maskSensitiveData(String sql) {
        if (!StringUtils.hasText(sql)) return sql;

        Matcher emailMatcher = EMAIL_PATTERN.matcher(sql);
        if (emailMatcher.find()) {
            sql = emailMatcher.replaceAll("'****@****'");
        }

        return sql;
    }

    //TODO: 개인정보 관련 마스킹 처리
}