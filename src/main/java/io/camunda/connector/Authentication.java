package io.camunda.connector;

import io.camunda.connector.api.ConnectorInput;
import io.camunda.connector.api.SecretStore;
import io.camunda.connector.api.Validator;
import java.util.Objects;

public class Authentication implements ConnectorInput {

  private String user;
  private String token;

  @Override
  public void validateWith(final Validator validator) {
    validator.require(user, "user");
    validator.require(token, "token");
    if (token != null && !token.startsWith("xobx")) {
      validator.addErrorMessage("Token must start with \"xobx\"");
    }
  }

  @Override
  public void replaceSecrets(final SecretStore secretStore) {
    token = secretStore.replaceSecret(token);
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public int hashCode() {
    return Objects.hash(token, user);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Authentication other = (Authentication) obj;
    return Objects.equals(token, other.token) && Objects.equals(user, other.user);
  }

  @Override
  public String toString() {
    return "Authentication [user=" + user + ", token=" + token + "]";
  }
}
