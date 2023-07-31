package io.camunda.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record MyConnectorRequest(
    @NotEmpty String message, @Valid @NotNull Authentication authentication) {}
