package io.camunda.connector;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import org.junit.jupiter.api.Test;

public class MyFunctionTest {

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
      .variables(input)
      .build();
    // when
    var result = function.execute(context);
    // then
    assertThat(result)
      .isInstanceOf(MyConnectorResult.class)
      .extracting("myProperty")
      .isEqualTo("Message received: Hello World!");
  }
}