package com.ode.junior.server;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import static com.ode.junior.utils.EventMessageType.INCOMING_REQUEST;
import static io.vertx.core.json.JsonObject.mapFrom;

/**
 * Request handler that launches the detection of a change in the user's device.
 *
 * This handler is effective iff the user is authenticated so it needs to be added to the router after the
 * authentication handler.
 */
public class UserConnectionChangeDetectorHandler implements Handler<RoutingContext> {
  private final Vertx vertx;

  public UserConnectionChangeDetectorHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  public static Handler<RoutingContext> create(Vertx vertx) {
    return new UserConnectionChangeDetectorHandler(vertx);
  }

  @Override
  public void handle(RoutingContext context) {
    final HttpServerRequest request = context.request();
    final User user = context.user();
    if (user != null) {
      final String userPrincipal = user.subject();
      final String address = request.connection().remoteAddress().hostAddress();
      final String userAgent = context.request().getHeader("user-agent");
      final IncomingRequestDetails requestDetails = new IncomingRequestDetails(userPrincipal, address,
        userAgent, request.uri());
      vertx.eventBus().send(INCOMING_REQUEST, mapFrom(requestDetails));
    }
    context.next();
  }
}
