package io.camunda.dd.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakService {

    private final String keycloakUrl;
    private final String clientSecret;
    private final String clientId;
    private final HttpClient httpClient;

    private String accessToken;
    private LocalDateTime accessTokenExpiry;

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakService.class);
    private static final String TOKEN_ENDPOINT = "/auth/realms/camunda-platform/protocol/openid-connect/token";

    public KeycloakService(String keycloakUrl, String clientId, String clientSecret) {
        this.keycloakUrl = keycloakUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getAccessToken() {
        LOGGER.info("Entrou em getAccessToken {}, {}, {}", accessToken, accessTokenExpiry, keycloakUrl);
        if (accessToken != null && accessTokenExpiry != null && LocalDateTime.now().isBefore(accessTokenExpiry)) {
            return accessToken;
        }

        try {
            String url = keycloakUrl + TOKEN_ENDPOINT;

            String form = "grant_type=" + encode("client_credentials") +
                          "&client_id=" + encode(clientId) +
                          "&client_secret=" + encode(clientSecret);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to obtain access token. HTTP " + response.statusCode() + ": " + response.body());
            }

            JSONObject json = new JSONObject(response.body());
            accessToken = json.getString("access_token");
            int expiresIn = json.optInt("expires_in", 3600);
            accessTokenExpiry = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(expiresIn - 60);

            return accessToken;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error obtaining access token: " + e.getMessage(), e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public Map<String, Object> getTokenInfo() {
        return Map.of(
            "accessToken", accessToken,
            "expiry", accessTokenExpiry
        );
    }
}
