package io.camunda.example;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Objects;

public class MyConnectorRequest {

  @NotEmpty
  private String message;

  @Valid
  @NotNull
  private Authentication authentication;

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
