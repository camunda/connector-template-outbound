package io.camunda.example.dto;

import jakarta.validation.constraints.NotEmpty;

public record Authentication(@NotEmpty String user, @NotEmpty String token) {}
