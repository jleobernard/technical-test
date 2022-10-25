package com.ode.junior.configuration;

public enum ConnectionInformationType {
  NOTHING(""),
  DEVICE_TYPE("device"),
  DEVICE_BRAND("device_brand"),
  DEVICE_MODEL("device_model"),
  OS_NAME("OS_name"),
  OS_FAMILY("OS_family"),
  OS_PLATFORM("OS_platform"),
  OS_VERSION("OS_version"),
  CLIENT_TYPE("Client_type"),
  CLIENT_NAME("Client_name"),
  CLIENT_VERSION("Client_version"),
  IP("IP");

  private final String code;

  ConnectionInformationType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
