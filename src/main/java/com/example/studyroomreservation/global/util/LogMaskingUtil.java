package com.example.studyroomreservation.global.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMaskingUtil {

    private static final String[] SENSITIVE_KEYS = {"password", "pwd", "email", "phone", "name"};

    private static final String REGEX_TEMPLATE = "(?i)([\"']?(%s)[\"']?\\s*[:=]\\s*)([\"']?)([^\"'\\s,]+)([\"']?)";

    public static String mask(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        String maskedContent = content;
        for (String key : SENSITIVE_KEYS) {
            Pattern pattern = Pattern.compile(String.format(REGEX_TEMPLATE, key));
            Matcher matcher = pattern.matcher(maskedContent);

            if (matcher.find()) {
                maskedContent = matcher.replaceAll("$1$3****$5");
            }
        }
        return maskedContent;
    }
}