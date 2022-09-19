package io.camunda.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.connector.impl.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import org.junit.jupiter.api.Test;

public class MyRequestTest {

  @Test
  void shouldReplaceTokenSecretWhenReplaceSecrets() {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("Hello World!");
    input.setAuthentication(auth);
    auth.setToken("secrets.MY_TOKEN");
    auth.setUser("testuser");
    var context = OutboundConnectorContextBuilder.create()
      .secret("MY_TOKEN", "token value")
      .build();
    // when
    context.replaceSecrets(input);
    // then
    assertThat(input)
      .extracting("authentication")
      .extracting("token")
      .isEqualTo("token value");
  }

  @Test
  void shouldFailWhenValidate_NoAuthentication() {
    // given
    var input = new MyConnectorRequest();
    input.setMessage("Hello World!");
    var context = OutboundConnectorContextBuilder.create().build();
    // when
    assertThatThrownBy(() -> context.validate(input))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("authentication");
  }

  @Test
  void shouldFailWhenValidate_NoToken() {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("Hello World!");
    input.setAuthentication(auth);
    auth.setUser("testuser");
    var context = OutboundConnectorContextBuilder.create().build();
    // when
    assertThatThrownBy(() -> context.validate(input))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("token");
  }

  @Test
  void shouldFailWhenValidate_NoMesage() {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setAuthentication(auth);
    auth.setUser("testuser");
    auth.setToken("xobx-test");
    var context = OutboundConnectorContextBuilder.create().build();
    // when
    assertThatThrownBy(() -> context.validate(input))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("message");
  }

  @Test
  void shouldFailWhenValidate_TokenWrongPattern() {
    // given
    var input = new MyConnectorRequest();
    var auth = new Authentication();
    input.setMessage("foo");
    input.setAuthentication(auth);
    auth.setUser("testuser");
    auth.setToken("test");
    var context = OutboundConnectorContextBuilder.create().build();
    // when
    assertThatThrownBy(() -> context.validate(input))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("Token must start with \"xobx\"");
  }
}