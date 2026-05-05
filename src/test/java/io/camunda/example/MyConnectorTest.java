package io.camunda.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.runtime.core.outbound.operation.ConnectorOperations;
import io.camunda.connector.runtime.core.outbound.operation.OutboundConnectorOperationFunction;
import io.camunda.connector.runtime.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.connector.validation.impl.DefaultValidationProvider;
import io.camunda.example.model.EchoResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Happy-path unit test for the annotations-based connector using the standard
 * {@link OutboundConnectorContextBuilder}. The {@code operation} custom header
 * selects which {@code @Operation} method is dispatched.
 */
class MyConnectorTest {

  @Test
  void echoOperation_returnsExpectedResponse() {
    var connector = new MyConnector();
    var operations =
        ConnectorOperations.from(connector, new ObjectMapper(), new DefaultValidationProvider());
    var function = new OutboundConnectorOperationFunction(operations);

    var context =
        OutboundConnectorContextBuilder.create()
            .variables(Map.of(
                "message", "hello",
                "authentication", Map.of("user", "alice", "token", "t-123")))
            .header("operation", "echo")
            .build();

    Object result = function.execute(context);

    assertThat(result).isInstanceOf(EchoResponse.class);
    assertThat(((EchoResponse) result).myProperty()).isEqualTo("Message received: hello");
  }
}
