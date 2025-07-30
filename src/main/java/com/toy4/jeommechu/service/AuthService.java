package com.toy4.jeommechu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {
    private final RestTemplate restTemplate;
    private final String fastapiBaseUrl;

    public AuthService(
            RestTemplate restTemplate,
            @Value("${fastapi.url}") String fastapiBaseUrl
    ) {
        this.restTemplate   = restTemplate;
        this.fastapiBaseUrl = fastapiBaseUrl;
    }

    /**
     * Spring에서 발급한 JWT를 FastAPI에 전달합니다.
     */
    public void forwardToken(String token) {
        String url = fastapiBaseUrl + "/api/auth/login";  // FastAPI 쪽 매핑 경로
        System.out.println("[DEBUG] Calling FastAPI at: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        System.out.println("[DEBUG] FastAPI status: " + resp.getStatusCode());
        System.out.println("[DEBUG] FastAPI body:   " + resp.getBody());

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("FastAPI 연동 실패: " + resp.getStatusCode());
        }
    }
}
