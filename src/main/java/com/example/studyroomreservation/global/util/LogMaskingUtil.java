package com.example.studyroomreservation.global.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMaskingUtil {

    private static final String[] SENSITIVE_KEYS = {"password", "pwd", "email", "phone", "name"};
    private static final String REGEX_TEMPLATE = "(?i)([\"']?(%s)[\"']?\\s*[:=]\\s*)([\"']?)([^\"'\\s,]+)([\"']?)";

    private static final List<Pattern> MASKING_PATTERNS;

    static {
        MASKING_PATTERNS = new ArrayList<>();
        for (String key : SENSITIVE_KEYS) {
            MASKING_PATTERNS.add(Pattern.compile(String.format(REGEX_TEMPLATE, key)));
        }
    }

    public static String mask(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        String maskedContent = content;

        for (Pattern pattern : MASKING_PATTERNS) {
            Matcher matcher = pattern.matcher(maskedContent);
            if (matcher.find()) {
                maskedContent = matcher.replaceAll("$1$3****$5");
            }
        }
        return maskedContent;
    }
}