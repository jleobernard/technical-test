package com.ode.junior;

import com.ode.junior.configuration.ConnectionInformationType;
import com.ode.junior.parser.ConnectionParserVerticle;
import com.ode.junior.parser.UserConnectionForRoute;
import com.ode.junior.server.IncomingRequestDetails;
import com.ode.junior.utils.VertxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ode.junior.TestUtils.USER_AGENT_CHROME_DESKTOP;
import static com.ode.junior.utils.EventMessageType.INCOMING_REQUEST;
import static com.ode.junior.utils.EventMessageType.USER_CONNECTION_AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(VertxExtension.class)
public class TestParserVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new ConnectionParserVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void push_a_detail_nothing_when_user_agent_blank(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(USER_CONNECTION_AVAILABLE, message -> testContext.verify(() -> {
      UserConnectionForRoute payload = VertxUtils.getMessageBodyAs(message, UserConnectionForRoute.class);
      Map<ConnectionInformationType, String> details = payload.userConnection().details();
      assertEquals("/protected/zyx", payload.route());
      assertEquals("user@mail.com", payload.userConnection().user());
      assertEquals(2, details.size());
      assertEquals("true", details.get(ConnectionInformationType.NOTHING));
      assertEquals("127.0.0.1", details.get(ConnectionInformationType.IP));
      testContext.completeNow();
    }));
    final IncomingRequestDetails details = new IncomingRequestDetails("user@mail.com",
      "127.0.0.1", "    ", "/protected/zyx");
    eventBus.send(INCOMING_REQUEST, JsonObject.mapFrom(details));
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
  @Test
  void push_a_detail_nothing_when_user_agent_null(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(USER_CONNECTION_AVAILABLE, message -> testContext.verify(() -> {
      UserConnectionForRoute payload = VertxUtils.getMessageBodyAs(message, UserConnectionForRoute.class);
      Map<ConnectionInformationType, String> details = payload.userConnection().details();
      assertEquals("/protected/zyx", payload.route());
      assertEquals("user@mail.com", payload.userConnection().user());
      assertEquals(2, details.size());
      assertEquals("true", details.get(ConnectionInformationType.NOTHING));
      assertEquals("127.0.0.1", details.get(ConnectionInformationType.IP));
      testContext.completeNow();
    }));
    final IncomingRequestDetails details = new IncomingRequestDetails("user@mail.com",
      "127.0.0.1", null, "/protected/zyx");
    eventBus.send(INCOMING_REQUEST, JsonObject.mapFrom(details));
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
  @Test
  void push_a_detail_for_chrome(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(USER_CONNECTION_AVAILABLE, message -> testContext.verify(() -> {
      UserConnectionForRoute payload = VertxUtils.getMessageBodyAs(message, UserConnectionForRoute.class);
      Map<ConnectionInformationType, String> details = payload.userConnection().details();
      assertEquals("/protected/zyx", payload.route());
      assertEquals("user@mail.com", payload.userConnection().user());
      assertEquals(9, details.size());
      assertFalse(details.containsKey(ConnectionInformationType.NOTHING));
      assertEquals("Mac", details.get(ConnectionInformationType.DEVICE_BRAND));
      assertEquals("Computer", details.get(ConnectionInformationType.DEVICE_TYPE));
      assertEquals("Browser", details.get(ConnectionInformationType.CLIENT_TYPE));
      assertEquals("Chrome", details.get(ConnectionInformationType.CLIENT_NAME));
      assertEquals("106.0.0.0", details.get(ConnectionInformationType.CLIENT_VERSION));
      assertEquals("Mac OS X", details.get(ConnectionInformationType.OS_NAME));
      assertEquals("Mac OS X", details.get(ConnectionInformationType.OS_FAMILY));
      testContext.completeNow();
    }));
    final IncomingRequestDetails details = new IncomingRequestDetails("user@mail.com",
      "127.0.0.1", USER_AGENT_CHROME_DESKTOP, "/protected/zyx");
    eventBus.send(INCOMING_REQUEST, JsonObject.mapFrom(details));
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
     throw testContext.causeOfFailure();
    }
  }
}
