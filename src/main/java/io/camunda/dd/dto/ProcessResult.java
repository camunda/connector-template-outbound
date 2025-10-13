package io.camunda.dd.dto;

import java.util.Map;

public record ProcessResult(Map<String, Object> taskResult) {}
