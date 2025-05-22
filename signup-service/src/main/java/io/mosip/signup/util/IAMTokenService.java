package io.mosip.signup.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class IAMTokenService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String cachedToken;
    private Instant expiryTime;

    @Value("${mosip.iam.token-endpoint}")
    private String authTokenEndpoint;

    @Value("${mosip.iam.clientid}")
    private String clientId;

    @Value("${mosip.iam.clientsecret}")
    private String clientSecret;

    @Value("${mosip.iam.token-expiry:60}")
    private int tokenExpirySeconds;

    public synchronized String getToken() {
        if (cachedToken == null || Instant.now().isAfter(expiryTime)) {
            fetchToken();
        }
        return cachedToken;
    }

    private void fetchToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "client_credentials");

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(authTokenEndpoint, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            this.cachedToken = (String) response.getBody().get("access_token");
            this.expiryTime = Instant.now().plusSeconds(tokenExpirySeconds);
        } else {
            throw new RuntimeException("Failed to retrieve token from IAM");
        }
    }

}
