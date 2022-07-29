package io.camunda.connector;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.connector.test.ConnectorContextBuilder;
import org.junit.jupiter.api.Test;

public class MyFunctionTest {

  @Test
  void shouldReturnReceivedMessageWhenExecute() throws Exception {
    // given
    var input = new MyConnectorRequest();
    input.setMessage("Hello World!");
    input.setToken("test");
    var function = new MyConnectorFunction();
    var context = ConnectorContextBuilder.create()
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