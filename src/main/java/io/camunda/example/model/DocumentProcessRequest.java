package io.camunda.example.model;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DocumentProcessRequest(
    @NotNull
    @TemplateProperty(
        group = "document",
        label = "Document",
        description = "The document to process. Bound automatically from a process variable holding a document reference.")
    Document document,

    @TemplateProperty(
        group = "document",
        label = "Additional documents",
        description = "Optional list of further documents processed alongside the primary one.",
        optional = true)
    List<Document> additionalDocuments) {}
