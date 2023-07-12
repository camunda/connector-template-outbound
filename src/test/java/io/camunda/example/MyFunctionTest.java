package io.camunda.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import org.junit.jupiter.api.Test;

public class MyFunctionTest {

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReturnReceivedMessageWhenExecute() throws Exception {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("Hello World!");
    input.setAuthentication(auth);
    auth.setToken("xobx-test");
    auth.setUser("testuser");
    var function = new MyConnectorFunction();
    var context = OutboundConnectorContextBuilder.create()
      .variables(objectMapper.writeValueAsString(input))
      .build();
    // when
    var result = function.execute(context);
    // then
    assertThat(result)
      .isInstanceOf(MyConnectorResult.class)
      .extracting("myProperty")
      .isEqualTo("Message received: Hello World!");
  }

  @Test
  void shouldThrowWithErrorCodeWhenMessageStartsWithFail() throws Exception {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("Fail: unauthorized");
    input.setAuthentication(auth);
    auth.setToken("xobx-test");
    auth.setUser("testuser");
    var function = new MyConnectorFunction();
    var context = OutboundConnectorContextBuilder.create()
        .variables(objectMapper.writeValueAsString(input))
        .build();
    // when
    var result = catchThrowable(() -> function.execute(context));
    // then
    assertThat(result)
        .isInstanceOf(ConnectorException.class)
        .hasMessageContaining("started with 'fail'")
        .extracting("errorCode").isEqualTo("FAIL");
  }
}