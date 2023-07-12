package io.camunda.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.impl.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import org.junit.jupiter.api.Test;

public class MyRequestTest {

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReplaceTokenSecretWhenReplaceSecrets() throws JsonProcessingException {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("Hello World!");
    input.setAuthentication(auth);
    auth.setToken("secrets.MY_TOKEN");
    auth.setUser("testuser");
    var context = OutboundConnectorContextBuilder.create()
      .secret("MY_TOKEN", "token value")
            .variables(objectMapper.writeValueAsString(input))
      .build();
    // when
    final var connectorRequest = context.bindVariables(MyConnectorRequest.class);
    // then
    assertThat(connectorRequest)
      .extracting("authentication")
      .extracting("token")
      .isEqualTo("token value");
  }

  @Test
  void shouldFailWhenValidate_NoAuthentication() throws JsonProcessingException {
    // given
    var input = new MyConnectorRequest();
    input.setMessage("Hello World!");
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(MyConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("authentication");
  }

  @Test
  void shouldFailWhenValidate_NoToken() throws JsonProcessingException {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("Hello World!");
    input.setAuthentication(auth);
    auth.setUser("testuser");
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(MyConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("token");
  }

  @Test
  void shouldFailWhenValidate_NoMesage() throws JsonProcessingException {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setAuthentication(auth);
    auth.setUser("testuser");
    auth.setToken("xobx-test");
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(MyConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("message");
  }

  @Test
  void shouldFailWhenValidate_TokenEmpty() throws JsonProcessingException {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("foo");
    input.setAuthentication(auth);
    auth.setUser("testuser");
    auth.setToken("");
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(MyConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("authentication.token: Validation failed");
  }
}