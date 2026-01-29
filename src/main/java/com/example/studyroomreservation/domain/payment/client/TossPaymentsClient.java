package com.example.studyroomreservation.domain.payment.client;

import com.example.studyroomreservation.domain.payment.dto.request.TossConfirmRequest;
import com.example.studyroomreservation.domain.payment.dto.response.TossConfirmResponse;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final WebClient tossWebClient;

    public TossConfirmResponse confirm(TossConfirmRequest request) {
        try {
            return tossWebClient.post()
                    .uri("/v1/payments/confirm")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("toss error")
                                    .flatMap(body -> Mono.error(
                                            new BusinessException(ErrorCode.PAYMENT_FAILED, body)
                                    ))
                    )
                    .bodyToMono(TossConfirmResponse.class)
                    .block();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "toss confirm call failed", e);
        }
    }
}
