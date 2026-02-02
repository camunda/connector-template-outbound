package io.camunda.example.model;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record Authentication(
    @NotEmpty
    @TemplateProperty(group = "authentication", label = "Username", description = "The username for authentication")
    String user,
    @NotEmpty @TemplateProperty(group = "authentication", label = "Token", description = "The token for authentication")
    String token) { }
