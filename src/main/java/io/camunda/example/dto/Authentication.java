package io.camunda.example.dto;

import javax.validation.constraints.NotEmpty;

public record Authentication (@NotEmpty String user, @NotEmpty String token) {}
