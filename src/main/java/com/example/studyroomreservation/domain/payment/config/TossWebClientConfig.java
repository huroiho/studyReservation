package com.example.studyroomreservation.domain.payment.config;

import com.example.studyroomreservation.global.config.TossPaymentConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class TossWebClientConfig {

    @Bean
    public WebClient tossWebClient(WebClient.Builder builder, TossPaymentConfig tossPaymentConfig) {

        String secretKey = tossPaymentConfig.getSecretKey();
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return builder
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
