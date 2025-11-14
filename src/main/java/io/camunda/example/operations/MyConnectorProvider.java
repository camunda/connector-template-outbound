package io.camunda.example.operations;

import io.camunda.connector.api.annotation.Operation;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.annotation.Variable;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorProvider;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.dto.MyConnectorRequest;
import io.camunda.dto.MyConnectorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "MYCONNECTOR-v2",
    type = "io.camunda:template:2")
@ElementTemplate(
    id = "io.camunda.connector.Template.v2",
    name = "Template connector v2",
    version = 1,
    description = "Describe this connector",
    icon = "icon.svg",
    documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/"
)
public class MyConnectorProvider implements OutboundConnectorProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyConnectorProvider.class);

  @Operation(id = "sendBackMessage", name = "send back message")
  public Object executor(@Variable MyConnectorRequest connectorRequest) {
    // TODO: implement connector logic
    LOGGER.info("Executing my connector with request {}", connectorRequest);
    String message = connectorRequest.message();
    if (message != null && message.toLowerCase().startsWith("fail")) {
      throw new ConnectorException("FAIL", "My property started with 'fail', was: " + message);
    }
    return new MyConnectorResult("Message received: " + message);
  }

  @Operation(id = "addTwoNumbers", name = "add two numbers")
  public Object addTwoNumbers(@Variable(name = "A") int a, @Variable(name = "B") int b) {
    return a + b;
  }
}
