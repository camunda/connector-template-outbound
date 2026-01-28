package io.camunda.example;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.error.ConnectorRetryException;
import io.camunda.connector.api.error.ConnectorRetryExceptionBuilder;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.example.model.EchoRequest;
import io.camunda.example.model.EchoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
              .message("My property started with 'retry', was: " + message).build();
    }
    return new EchoResponse("Message received: " + message);
  }

  @Operation(id = "addTwoNumbers", name = "Add two numbers")
  public Object addTwoNumbers(@Variable(name = "A") int a, @Variable(name = "B") int b) {
    return a + b;
  }
}
