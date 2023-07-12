package io.camunda.example;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;

public class Authentication {

  @NotEmpty
  private String user;

  @NotEmpty
  private String token;

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
