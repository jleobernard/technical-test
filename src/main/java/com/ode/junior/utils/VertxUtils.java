package com.ode.junior.utils;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class VertxUtils {
  public static <T> T getMessageBodyAs(final Message<?> message, Class<T> clazz) {
    return ((JsonObject)message.body()).mapTo(clazz);
  }
}
