package io.camunda.example.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EchoRequest(
    @NotEmpty @TemplateProperty(group = "message", type = PropertyType.Text) String message,
    @Valid @NotNull Authentication authentication) {}
