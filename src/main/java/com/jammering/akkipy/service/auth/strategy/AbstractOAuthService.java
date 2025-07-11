package com.jammering.akkipy.service.auth.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractOAuthService implements AuthStrategy {

    protected final RestTemplate restTemplate;
    protected final ObjectMapper objectMapper;

    public AbstractOAuthService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    protected HttpHeaders defaultHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        return headers;
    }
}