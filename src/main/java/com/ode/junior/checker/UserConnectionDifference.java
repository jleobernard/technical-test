package com.ode.junior.checker;

import java.util.Objects;

public record UserConnectionDifference(String code, String referenceValue, String requestValue) {
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    UserConnectionDifference that = (UserConnectionDifference) o;
    return code.equals(that.code) && referenceValue.equals(that.referenceValue) && requestValue.equals(that.requestValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, referenceValue, requestValue);
  }
}
