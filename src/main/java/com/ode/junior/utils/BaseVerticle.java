package com.ode.junior.utils;

import com.ode.junior.configuration.Configuration;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public abstract class BaseVerticle extends AbstractVerticle {
  protected Configuration configuration;
  protected EventBus eventBus;

  @Override
  public void start(final Promise<Void> startPromise) {
    ConfigRetriever.create(vertx).getConfig(configurationRetriever -> {
      if (configurationRetriever.failed()) {
        startPromise.fail("configuration.retrieval.error");
      } else {
        this.configuration = configurationRetriever.result().mapTo(Configuration.class);
        this.eventBus = vertx.eventBus();
        this.startWithConfiguration(startPromise);
      }
    });
  }

  protected abstract void startWithConfiguration(Promise<Void> startPromise);

  protected void send(final String messagetype, final Object t) {
    this.eventBus.send(messagetype, JsonObject.mapFrom(t));
  }
}
