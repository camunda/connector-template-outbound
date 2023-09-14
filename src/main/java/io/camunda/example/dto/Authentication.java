package io.camunda.example.dto;

import io.camunda.connector.generator.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record Authentication(
    @NotEmpty
    @TemplateProperty(group = "authentication", label = "Username", description = "The username for authentication")
    String user,
    @NotEmpty @TemplateProperty(group = "authentication", description = "The token for authentication")
    String token) { }
