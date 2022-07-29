package io.camunda.connector;

import io.camunda.connector.api.SecretStore;
import io.camunda.connector.api.Validator;
import java.util.Objects;

public class MyConnectorRequest {

  private String message;
  private String token;

  public void validateWith(final Validator validator) {
    validator.require(message, "message");
    validator.require(token, "token");
  }

  public void replaceSecrets(final SecretStore secretStore) {
    token = secretStore.replaceSecret(token);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, token);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MyConnectorRequest other = (MyConnectorRequest) obj;
    return Objects.equals(message, other.message) && Objects.equals(token, other.token);
  }

  @Override
  public String toString() {
    return "MyConnectorRequest [message=" + message + ", token=" + token + "]";
  }

}
