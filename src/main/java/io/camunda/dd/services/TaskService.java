package io.camunda.dd.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    private static final int MAX_ATTEMPTS = 30;
    
    private final String tasklistBaseUrl;
    private final KeycloakService keycloakService;
    private final HttpClient httpClient;

    private static final String TASKS_PATH = "/tasks";
    private static final String SEARCH_PATH = "/search";

    public TaskService(String tasklistBaseUrl, KeycloakService keycloakService) {
        this.tasklistBaseUrl = tasklistBaseUrl;
        this.keycloakService = keycloakService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Map<String, Object> waitForTaskReady(String state, Long processInstanceKey) throws InterruptedException {
        LOGGER.info("Entrou em waitForTaskReady {}, {}", state, processInstanceKey);
        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            boolean lastAttempt = (attempts == MAX_ATTEMPTS - 1);

            Map<String, Object> taskPayload = getTask(state, processInstanceKey, lastAttempt);
            if (taskPayload != null && !taskPayload.isEmpty()) {
                return taskPayload;
            }

            LOGGER.info(String.format("Task not found for process instance %s - (attempt %d/%d)",
                    processInstanceKey, attempts + 1, MAX_ATTEMPTS));
            attempts++;
            Thread.sleep(2000);
        }
        return null;
    }

    public Map<String, Object> getTask(String state, Long processInstanceKey, boolean useTaskState) {
        LOGGER.info("Entrou em getTask {}, {}, {}", state, processInstanceKey, useTaskState);
        List<Map<String, Object>> tasks = performTaskSearch(processInstanceKey, state, useTaskState);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks.get(0);
        } else {
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> performTaskSearch(Long processInstance, String state, boolean useTaskState) {
        LOGGER.info("Entrou em performTaskSearch {}, {}, {}", processInstance, state, useTaskState);
        String url = tasklistBaseUrl + TASKS_PATH + SEARCH_PATH;
        LOGGER.info("url {}", url);

        JSONObject body = new JSONObject();
        if (useTaskState) {
            body.put("taskState", state);
        } else {
            body.put("state", state);
        }
        body.put("processInstanceKey", processInstance.toString().trim());

        try {
            String token = keycloakService.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                List<Map<String, Object>> resultList = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Map<String, Object> map = obj.toMap();
                    resultList.add(map);
                }
                return resultList;
            } else {
                LOGGER.info("HTTP " + response.statusCode() + " - " + response.body());
                return Collections.emptyList();
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.info("Task not found for process instance " + processInstance + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
