package io.camunda.example.model;

public record DocumentProcessResponse(
    String fileName,
    String contentType,
    long size,
    String sha256,
    String strategy,
    int additionalDocumentCount) {}
