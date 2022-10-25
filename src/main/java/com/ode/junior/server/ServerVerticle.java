package com.ode.junior.server;

import com.ode.junior.utils.BaseVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BasicAuthHandler;

public class ServerVerticle extends BaseVerticle {
  private static final Logger log = LoggerFactory.getLogger(ServerVerticle.class);

  @Override
  public void startWithConfiguration(final Promise<Void> startPromise) {
    final AuthenticationProvider provider = new AuthenticationProvider(this.configuration);
    final Router router = Router.router(vertx);
    router.route("/protected/*").handler(BasicAuthHandler.create(provider));
    router.route("/protected/*").handler(UserConnectionChangeDetectorHandler.create(vertx));
    router.get("/protected/*").handler(this::displayProtectedContent);
    router.get("/public/*").handler(this::displayPublicContent);
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(this.configuration.server().port())
      .onSuccess(server -> {
        log.info("HTTP server started on port " + this.configuration.server().port());
        startPromise.complete();
      }).onFailure(failure -> startPromise.fail(failure.getCause()));
  }

  private void displayPublicContent(RoutingContext context) {
    context.json(new JsonObject()
      .put("success", true)
      .put("message", "Welcome to " + context.request().path()));
  }

  private void displayProtectedContent(RoutingContext context) {
    context.json(new JsonObject()
      .put("success", true)
      .put("message", "Access granted to " + context.user().subject() + " to path " + context.request().path()));
  }
}
