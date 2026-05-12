package io.camunda.example;

import io.camunda.connector.api.document.Document;
import io.camunda.connector.api.document.DocumentLinkParameters;
import io.camunda.connector.api.document.DocumentMetadata;
import io.camunda.connector.api.document.DocumentReference;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.example.model.DocumentProcessRequest;
import io.camunda.example.model.DocumentProcessResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessDocumentTest {

  private final MyConnector connector = new MyConnector();

  @Test
  void smallDocument_processedInMemory() {
    byte[] content = "hello world".getBytes();
    var doc = new TestDocument(content, "hello.txt", "text/plain", (long) content.length);

    DocumentProcessResponse response =
        connector.processDocument(new DocumentProcessRequest(doc, List.of()));

    assertThat(response.fileName()).isEqualTo("hello.txt");
    assertThat(response.contentType()).isEqualTo("text/plain");
    assertThat(response.size()).isEqualTo(content.length);
    assertThat(response.strategy()).isEqualTo("in-memory");
    assertThat(response.sha256()).isEqualTo(expectedSha256(content));
    assertThat(response.additionalDocumentCount()).isZero();
  }

  @Test
  void largeDocument_processedStreaming_doesNotLoadFully() {
    // 4 MiB > IN_MEMORY_THRESHOLD_BYTES (1 MiB) → streaming path is taken.
    byte[] content = new byte[4 * 1024 * 1024];
    for (int i = 0; i < content.length; i++) content[i] = (byte) (i % 251);
    var doc = new TestDocument(content, "big.bin", "application/octet-stream", (long) content.length) {
      @Override
      public byte[] asByteArray() {
        // Fail loudly if the connector takes the in-memory path on a large document.
        throw new AssertionError("Large documents must be streamed, not loaded into memory");
      }
    };

    DocumentProcessResponse response =
        connector.processDocument(new DocumentProcessRequest(doc, List.of()));

    assertThat(response.strategy()).isEqualTo("stream");
    assertThat(response.size()).isEqualTo(content.length);
    assertThat(response.sha256()).isEqualTo(expectedSha256(content));
  }

  @Test
  void documentExceedingMaxSize_isRejected() {
    var doc = new TestDocument(new byte[0], "huge.bin", "application/octet-stream",
        MyConnector.MAX_DOCUMENT_SIZE_BYTES + 1);

    assertThatThrownBy(() -> connector.processDocument(new DocumentProcessRequest(doc, List.of())))
        .isInstanceOf(ConnectorException.class)
        .extracting(t -> ((ConnectorException) t).getErrorCode())
        .isEqualTo("DOCUMENT_TOO_LARGE");
  }

  @Test
  void multipleDocuments_arePassedThrough() {
    byte[] primary = "primary".getBytes();
    var doc = new TestDocument(primary, "a.txt", "text/plain", (long) primary.length);
    var extra1 = new TestDocument("e1".getBytes(), "e1.txt", "text/plain", 2L);
    var extra2 = new TestDocument("e2".getBytes(), "e2.txt", "text/plain", 2L);

    DocumentProcessResponse response =
        connector.processDocument(new DocumentProcessRequest(doc, List.of(extra1, extra2)));

    assertThat(response.additionalDocumentCount()).isEqualTo(2);
  }

  private static String expectedSha256(byte[] bytes) {
    try {
      return HexFormat.of()
          .formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Minimal in-memory Document for unit tests — avoids spinning up a runtime. */
  private static class TestDocument implements Document {
    private final byte[] content;
    private final DocumentMetadata metadata;

    TestDocument(byte[] content, String fileName, String contentType, Long declaredSize) {
      this.content = content;
      this.metadata = new DocumentMetadata() {
        @Override public String getContentType() { return contentType; }
        @Override public OffsetDateTime getExpiresAt() { return null; }
        @Override public Long getSize() { return declaredSize; }
        @Override public String getFileName() { return fileName; }
        @Override public String getProcessDefinitionId() { return null; }
        @Override public Long getProcessInstanceKey() { return null; }
        @Override public Map<String, Object> getCustomProperties() { return Map.of(); }
      };
    }

    @Override public DocumentMetadata metadata() { return metadata; }
    @Override public String asBase64() { return java.util.Base64.getEncoder().encodeToString(content); }
    @Override public InputStream asInputStream() { return new ByteArrayInputStream(content); }
    @Override public byte[] asByteArray() { return content; }
    @Override public DocumentReference reference() { return null; }
    @Override public String generateLink(DocumentLinkParameters params) { return null; }
  }
}
