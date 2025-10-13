package io.camunda.dd.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperateService.class);
    private static final String SEARCH_PATH = "/search";
    private static final int MAX_ATTEMPTS = 30;

    private final String operateBaseUrl;
    private final KeycloakService keycloakService;
    private final HttpClient httpClient;

    public OperateService(String operateBaseUrl, KeycloakService keycloakService) {
        this.operateBaseUrl = operateBaseUrl;
        this.keycloakService = keycloakService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Long searchProcessInstance(Long initialParentKey) {
        final String url = operateBaseUrl + SEARCH_PATH;

        long currentParentKey = initialParentKey;
        int hops = 0;

        Long lastNonEmptyKey = null;
        List<Long> triedParentKeys = new ArrayList<>();

        while (true) {
            if (hops >= MAX_ATTEMPTS) {
                String msg = String.format(
                        "Reached maximum loop (%d) while searching for process instance from %d",
                        MAX_ATTEMPTS, initialParentKey
                );
                LOGGER.info(msg);
                throw new RuntimeException(msg);
            }

            triedParentKeys.add(currentParentKey);

            String jsonBody = String.format(
                    "{\"filter\": {\"parentKey\": %d, \"state\": \"ACTIVE\"}}",
                    currentParentKey
            );

            try {
                String accessToken = keycloakService.getAccessToken();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(5))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    String msg = "HTTP " + response.statusCode() + ": " + response.body();
                    LOGGER.info(msg);
                    if (lastNonEmptyKey != null) {
                        return lastNonEmptyKey;
                    }
                    throw new RuntimeException(msg);
                }

                Map<String, Object> responseBody = parseJsonToMap(response.body());
                List<Map<String, Object>> items = extractItemsList(responseBody);

                if (items == null || items.isEmpty()) {
                    String msg = String.format("Empty response. ParentKeys tried so far: %s", triedParentKeys);
                    LOGGER.info(msg);
                    if (lastNonEmptyKey != null) {
                        LOGGER.info("Returning last non-empty response=" + lastNonEmptyKey);
                        return lastNonEmptyKey;
                    }
                    throw new RuntimeException(msg);
                }

                Object keyObj = items.get(0).get("key");
                if (keyObj == null) {
                    String msg = String.format("First item without a key. ParentKeys tried so far: %s", triedParentKeys);
                    LOGGER.info(msg);
                    if (lastNonEmptyKey != null) {
                        return lastNonEmptyKey;
                    }
                    throw new RuntimeException(msg);
                }

                long returnedKey = (keyObj instanceof Number)
                        ? ((Number) keyObj).longValue()
                        : Long.parseLong(keyObj.toString());

                lastNonEmptyKey = returnedKey;

                if (returnedKey == currentParentKey) {
                    return returnedKey;
                }

                currentParentKey = returnedKey;
                hops++;

            } catch (IOException | InterruptedException e) {
                LOGGER.info("Error while calling Operate API: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
    
    private Map<String, Object> parseJsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;

        int startIdx = json.indexOf("[");
        int endIdx = json.lastIndexOf("]");
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            String innerArray = json.substring(startIdx + 1, endIdx);
            List<Map<String, Object>> items = new ArrayList<>();

            String[] objects = innerArray.split("\\},\\{");
            for (String obj : objects) {
                obj = obj.replaceAll("[\\[\\]\\{\\}]", "");
                Map<String, Object> innerMap = new HashMap<>();
                String[] pairs = obj.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].replaceAll("\"", "").trim();
                        String value = kv[1].replaceAll("\"", "").trim();
                        innerMap.put(key, value);
                    }
                }
                items.add(innerMap);
            }
            map.put("items", items);
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItemsList(Map<String, Object> body) {
        Object obj = body.get("items");
        if (obj instanceof List) {
            return (List<Map<String, Object>>) obj;
        }
        return Collections.emptyList();
    }
}
