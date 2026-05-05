package io.camunda.example;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.document.Document;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.error.ConnectorRetryExceptionBuilder;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.example.model.DocumentProcessRequest;
import io.camunda.example.model.DocumentProcessResponse;
import io.camunda.example.model.EchoRequest;
import io.camunda.example.model.EchoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@OutboundConnector(name = "My Connector", type = "io.camunda:example:1")
@ElementTemplate(
    id = "io.camunda.example.template.v1",
    name = "My Connector Template",
    version = 1,
    description = "This is the description of my connector example.",
    icon = "icon.svg",
    documentationRef =
        "https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/"
)
public class MyConnector implements OutboundConnectorProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyConnector.class);

  // Just for demonstration purposes, retries should be managed properly in a real connector
  private static int RETRIES = 3;

  @Operation(id = "echo", name = "Echo message")
  public Object echo(@Variable EchoRequest echoRequest) {
    LOGGER.info("Executing my connector with request {}", echoRequest);
    String message = echoRequest.message();
    if (message.toLowerCase().startsWith("fail")) {
      // Simulate a non-retryable error
      throw new ConnectorException("FAIL", "My property started with 'fail', was: " + message);
    } else if(message.toLowerCase().startsWith("retry")){
      // Simulate a retryable error
      throw new ConnectorRetryExceptionBuilder()
              .errorCode("RETRY")
              .message("My property started with 'retry', was: " + message)
              .retries(RETRIES--)
              .backoffDuration(Duration.ofSeconds(1))
              .build();
    }
    return new EchoResponse("Message received: " + message);
  }

  @Operation(id = "addTwoNumbers", name = "Add two numbers")
  public Object addTwoNumbers(@Variable(name = "A") int a, @Variable(name = "B") int b) {
    return a + b;
  }

  // Documents above this size are streamed to avoid loading the entire payload into the
  // shared connector-runtime heap. Tune to your runtime's heap budget.
  static final long IN_MEMORY_THRESHOLD_BYTES = 1L * 1024 * 1024; // 1 MiB
  // Hard ceiling: refuse anything larger so a single job cannot exhaust the runtime.
  static final long MAX_DOCUMENT_SIZE_BYTES = 100L * 1024 * 1024; // 100 MiB

  @Operation(id = "processDocument", name = "Process document")
  public DocumentProcessResponse processDocument(@Variable DocumentProcessRequest request) {
    Document doc = request.document();
    long size = Optional.ofNullable(doc.metadata().getSize()).orElse(-1L);

    if (size > MAX_DOCUMENT_SIZE_BYTES) {
      throw new ConnectorException(
          "DOCUMENT_TOO_LARGE",
          "Document size " + size + " exceeds limit " + MAX_DOCUMENT_SIZE_BYTES);
    }

    String strategy;
    String sha256;
    if (size >= 0 && size <= IN_MEMORY_THRESHOLD_BYTES) {
      // Small documents: in-memory bytes are simple and fine.
      strategy = "in-memory";
      sha256 = sha256(doc.asByteArray());
    } else {
      // Large or unknown size: stream to bound memory use.
      strategy = "stream";
      sha256 = sha256Stream(doc.asInputStream());
    }

    List<Document> additional = Optional.ofNullable(request.additionalDocuments()).orElse(List.of());
    return new DocumentProcessResponse(
        doc.metadata().getFileName(),
        doc.metadata().getContentType(),
        size,
        sha256,
        strategy,
        additional.size());
  }

  private static String sha256(byte[] bytes) {
    return HexFormat.of().formatHex(newSha256().digest(bytes));
  }

  private static String sha256Stream(InputStream in) {
    MessageDigest digest = newSha256();
    byte[] buf = new byte[8 * 1024];
    try (InputStream stream = in) {
      int read;
      while ((read = stream.read(buf)) != -1) {
        digest.update(buf, 0, read);
      }
    } catch (IOException e) {
      throw new ConnectorException("DOCUMENT_READ_FAILED", "Failed to read document stream", e);
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private static MessageDigest newSha256() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
