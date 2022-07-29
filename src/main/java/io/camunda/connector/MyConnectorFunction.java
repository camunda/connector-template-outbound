package io.camunda.connector;

import io.camunda.connector.api.ConnectorContext;
import io.camunda.connector.api.ConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConnectorFunction implements ConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyConnectorFunction.class);

  @Override
  public Object execute(ConnectorContext context) throws Exception {
    var connectorRequest = context.getVariablesAsType(MyConnectorRequest.class);

    context.validate(connectorRequest);
    context.replaceSecrets(connectorRequest);

    return executeConnector(connectorRequest);
  }

  private MyConnectorResult executeConnector(final MyConnectorRequest connectorRequest) {
    // TODO: implement connector logic
    LOGGER.info("Executing my connector with request {}", connectorRequest);
    var result = new MyConnectorResult();
    result.setMyProperty("Message received: " + connectorRequest.getMessage());
    return result;
  }
}
