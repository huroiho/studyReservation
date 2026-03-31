package com.example.studyroomreservation.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TossPaymentConfig {

    @Value("${toss.client-key}")
    private String clientKey;

    @Value("${toss.secret-key}")
    private String secretKey;
}