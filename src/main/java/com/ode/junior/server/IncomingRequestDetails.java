package com.ode.junior.server;

public record IncomingRequestDetails(String user, String ip, String userAgent, String route) {
  @Override
  public String toString() {
    return "IncomingConnectionDetails{" +
      "user='" + user + '\'' +
      ", ip='" + ip + '\'' +
      ", userAgent='" + userAgent + '\'' +
      ", route='" + route + '\'' +
      '}';
  }
}
