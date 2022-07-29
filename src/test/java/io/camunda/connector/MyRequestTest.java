package io.camunda.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.connector.api.Validator;
import io.camunda.connector.test.ConnectorContextBuilder;
import org.junit.jupiter.api.Test;

public class MyRequestTest {

  @Test
  void shouldReplaceTokenSecretWhenReplaceSecrets() {
    // given
    var input = new MyConnectorRequest();
    input.setMessage("Hello World!");
    input.setToken("secrets.MY_TOKEN");
    var context = ConnectorContextBuilder.create()
      .secret("MY_TOKEN", "token value")
      .build();
    // when
    input.replaceSecrets(context.getSecretStore());
    // then
    assertThat(input)
      .extracting("token")
      .isEqualTo("token value");
  }

  @Test
  void shouldFailWhenValidate_NoToken() {
    // given
    var input = new MyConnectorRequest();
    input.setMessage("Hello World!");
    var validator = new Validator();
    input.validateWith(validator);
    // when
    assertThatThrownBy(() -> validator.evaluate())
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Property 'token' is missing");
  }

  @Test
  void shouldFailWhenValidate_NoMesage() {
    // given
    var input = new MyConnectorRequest();
    input.setToken("test");
    var validator = new Validator();
    input.validateWith(validator);
    // when
    assertThatThrownBy(() -> validator.evaluate())
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Property 'message' is missing");
  }
}