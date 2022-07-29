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
    context.replaceSecrets(input);
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
      .hasMessageContaining("token");
  }

  @Test
  void shouldFailWhenValidate_NoMesage() {
    // given
    var input = new MyConnectorRequest();
    input.setToken("xobx-test");
    var validator = new Validator();
    input.validateWith(validator);
    // when
    assertThatThrownBy(() -> validator.evaluate())
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("message");
  }

  @Test
  void shouldFailWhenValidate_TokenWrongPattern() {
    // given
    var input = new MyConnectorRequest();
    input.setToken("test");
    input.setMessage("foo");
    var validator = new Validator();
    input.validateWith(validator);
    // when
    assertThatThrownBy(() -> validator.evaluate())
      // then
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Token must start with \"xobx\"");
  }
}