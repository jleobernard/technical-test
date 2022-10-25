package com.ode.junior;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

import static com.ode.junior.utils.EventMessageType.USER_CONNECTION_AVAILABLE;

public class TestUtils {
  public static final String USER_AGENT_CHROME_DESKTOP = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36";
  public static final String USER_AGENT_SAFARI_DESKTOP = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.6 Safari/605.1.15";
  public static final String USER_AGENT_FIREFOX_DESKTOP = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:106.0) Gecko/20100101 Firefox/106.0";

  public static Future<?> sendMessagesSequentially(List<Object> messages, Vertx vertx) {
    final EventBus eventBus = vertx.eventBus();
    final Future<?> allMessagesAckedFuture;
    if(messages.isEmpty()) {
      allMessagesAckedFuture = Future.succeededFuture();
    } else {
      final Object message = messages.get(0);
      allMessagesAckedFuture = eventBus.request(USER_CONNECTION_AVAILABLE, JsonObject.mapFrom(message))
        .compose(lastResponse -> {
          final List<Object> remainingMessages = messages.stream().skip(1).collect(Collectors.toList());
          return sendMessagesSequentially(remainingMessages, vertx);
        });
    }
    return allMessagesAckedFuture;
  }
}
