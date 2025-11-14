package io.camunda.example.classic.integration;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;

@SpringBootTest(
    classes = {TestConnectorRuntimeApplication.class},
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CamundaSpringProcessTest
public class MyConnectorProviderIntegrationTest {

  @Autowired
  private CamundaClient client;

  @Test
  void testMyConnectorFunctionality() {
    // given - create a process instance with a correlation variable
    final var processInstance =
        client
            .newCreateInstanceCommand()
            // processes in resources/bpmn are automatically deployed
            .bpmnProcessId("operation-connector-test-process")
            .latestVersion()
            .send()
            .join();

    // The connector will automatically generate events and correlate them
    // Verify the process completes
    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byName("MyConnector"))
        .hasVariable("result", Map.of("myProperty", "Message received: Hello Camunda"))
        .isCompleted();
  }
}
