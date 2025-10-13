package io.camunda.dd.dto;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty.PropertyType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProcessRequest(
    @NotNull @TemplateProperty(type = PropertyType.Text) Long processInstance,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String state,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String tasklistAddress,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String operateAddress,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String keycloakAddress,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String clientSecret,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String clientId) {}
