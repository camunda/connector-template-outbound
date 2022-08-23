package io.camunda.connector;

import io.camunda.connector.api.ConnectorInput;
import io.camunda.connector.api.SecretStore;
import io.camunda.connector.api.Validator;
import java.util.Objects;

public class MyConnectorRequest implements ConnectorInput {

  private String message;
  private Authentication authentication;

  @Override
  public void validateWith(final Validator validator) {
    validator.require(message, "message");
    validator.require(authentication, "authentication");
    validateIfNotNull(authentication, validator);
  }

  @Override
  public void replaceSecrets(final SecretStore secretStore) {
    replaceSecretsIfNotNull(authentication, secretStore);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  @Override
  public int hashCode() {
    return Objects.hash(authentication, message);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MyConnectorRequest other = (MyConnectorRequest) obj;
    return Objects.equals(authentication, other.authentication)
        && Objects.equals(message, other.message);
  }

  @Override
  public String toString() {
    return "MyConnectorRequest [message=" + message + ", authentication=" + authentication + "]";
  }
}
