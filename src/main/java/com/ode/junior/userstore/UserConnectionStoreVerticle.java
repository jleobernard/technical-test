package com.ode.junior.userstore;

import com.ode.junior.parser.UserConnection;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import static com.ode.junior.utils.EventMessageType.GET_CONNECTION_DETAILS;
import static com.ode.junior.utils.EventMessageType.UPDATE_CONNECTION_DETAILS;

public class UserConnectionStoreVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(UserConnectionStoreVerticle.class);
  private Map<String, UserConnection> store;

  @Override
  public void start() {
    store = new HashMap<>();
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(GET_CONNECTION_DETAILS, this::fetchUserConnectionDetails);
    eventBus.consumer(UPDATE_CONNECTION_DETAILS, this::updateUserConnectionDetails);
  }

  private void updateUserConnectionDetails(Message<JsonObject> message) {
    final UserConnection userConnection = message.body().mapTo(UserConnection.class);
    log.debug("Storing " + userConnection.user() +"'s first connection");
    store.put(userConnection.user(), userConnection);
  }

  private void fetchUserConnectionDetails(Message<String> message) {
    final String userPrincipal = message.body();
    log.debug("Retrieving " + userPrincipal +"'s connection details");
    message.reply(JsonObject.mapFrom(store.get(userPrincipal)));
  }
}
