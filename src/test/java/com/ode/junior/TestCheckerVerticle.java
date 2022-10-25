package com.ode.junior;

import com.ode.junior.checker.Alert;
import com.ode.junior.checker.UserConnectionCheckerVerticle;
import com.ode.junior.checker.UserConnectionDifference;
import com.ode.junior.configuration.ConnectionInformationType;
import com.ode.junior.parser.UserConnection;
import com.ode.junior.parser.UserConnectionForRoute;
import com.ode.junior.userstore.UserConnectionStoreVerticle;
import com.ode.junior.utils.VertxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ode.junior.TestUtils.sendMessagesSequentially;
import static com.ode.junior.utils.EventMessageType.ALERT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestCheckerVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new UserConnectionCheckerVerticle())
    .compose(dpl -> vertx.deployVerticle(new UserConnectionStoreVerticle()))
    .onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void do_not_raise_alert_for_no_changes(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final UserConnection userConnection = new UserConnection("user@foo.com",
      Map.of(ConnectionInformationType.IP, "110.0.1.2",
        ConnectionInformationType.OS_NAME, "Mac"));
    final UserConnectionForRoute conn1 = new UserConnectionForRoute(
      userConnection,
      "/protected/route1"
    );
    final UserConnectionForRoute conn2 = new UserConnectionForRoute(
      userConnection,
      "/protected/route2"
    );
    sendMessagesSequentially(List.of(conn1, conn2), vertx);
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ALERT, message -> testContext.verify(() ->
      testContext.failNow("Should not have raised an alert")
    ));
    testContext.awaitCompletion(3, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
    testContext.completeNow();
  }

  @Test
  void do_not_raise_alert_for_small_changes(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final UserConnection userConnection1 = new UserConnection("user@foo.com",
      Map.of(ConnectionInformationType.IP, "110.0.1.2",
        ConnectionInformationType.OS_NAME, "Mac"));
    final UserConnection userConnection2 = new UserConnection("user@foo.com",
      Map.of(ConnectionInformationType.IP, "110.0.1.3",
        ConnectionInformationType.OS_NAME, "Mac"));
    final UserConnectionForRoute conn1 = new UserConnectionForRoute(
      userConnection1,
      "/protected/route1"
    );
    final UserConnectionForRoute conn2 = new UserConnectionForRoute(
      userConnection2,
      "/protected/route2"
    );
    sendMessagesSequentially(List.of(conn1, conn2), vertx);
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ALERT, message -> testContext.verify(() ->
      testContext.failNow("Should not have raised an alert")
    ));
    testContext.awaitCompletion(3, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
    testContext.completeNow();
  }

  @Test
  void raise_alert_for_big_changes(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final UserConnection userConnection1 = new UserConnection("user@foo.com",
      Map.of(ConnectionInformationType.IP, "110.0.1.2",
        ConnectionInformationType.OS_NAME, "Mac"));
    final UserConnection userConnection2 = new UserConnection("user@foo.com",
      Map.of(ConnectionInformationType.IP, "110.0.1.3",
        ConnectionInformationType.OS_NAME, "Windows"));
    final UserConnectionForRoute conn1 = new UserConnectionForRoute(
      userConnection1,
      "/protected/route1"
    );
    final UserConnectionForRoute conn2 = new UserConnectionForRoute(
      userConnection2,
      "/protected/route2"
    );
    sendMessagesSequentially(List.of(conn1, conn2), vertx);
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ALERT, message -> testContext.verify(() -> {
      final Alert alert = VertxUtils.getMessageBodyAs(message, Alert.class);
      List<UserConnectionDifference> details = alert.details();
      assertEquals("/protected/route2", alert.route());
      assertEquals(5, alert.score());
      assertEquals(2, details.size());
      final Set<String> codes = details.stream().map(UserConnectionDifference::code).collect(Collectors.toSet());
      assertEquals(Set.of(ConnectionInformationType.IP.getCode(), ConnectionInformationType.OS_NAME.getCode()), codes);
      testContext.completeNow();
    }));
    testContext.awaitCompletion(3, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }

}
