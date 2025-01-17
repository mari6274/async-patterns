package dev.maczkowski.jugpoznan.asyncpatterns.partnerapi;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerApiClient {

    private final RestClient restClient;

    public void submitApplication(String body) {
        restClient.post()
                .uri("/submitApplication")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
