package io.camunda.example.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Objects;

public record MyConnectorRequest (
        @NotEmpty
        String message,
        @Valid
        @NotNull
        Authentication authentication) {}
