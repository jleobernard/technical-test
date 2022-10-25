package com.ode.junior;

import com.ode.junior.alert.AlertVerticle;
import com.ode.junior.checker.Alert;
import com.ode.junior.checker.UserConnectionDifference;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ode.junior.utils.EventMessageType.ALERT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestAlertVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.fileSystem().delete("alert-test.log")
      .onComplete(completion -> vertx.deployVerticle(
        new AlertVerticle(),
        testContext.succeeding(id -> testContext.completeNow())));
  }

  @Test
  void write_to_log_file_when_alert_triggered(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final List<UserConnectionDifference> details = List.of(
      new UserConnectionDifference("Client_type", "navigateur", "app mobile"),
      new UserConnectionDifference("OS_version", "1.2.3", "4.5.6")
    );
    final Alert newAlert = new Alert("user@mail.com", "/protected/abc", 5, details, System.currentTimeMillis());
    final EventBus eventBus = vertx.eventBus();
    eventBus.request(ALERT, JsonObject.mapFrom(newAlert), message -> testContext.verify(() ->
      vertx.fileSystem().readFile("alert-test.log").onComplete(ar -> {
        final Alert writtenAlert = ar.result().toJsonObject().mapTo(Alert.class);
        assertEquals(newAlert.user(), writtenAlert.user());
        assertEquals(newAlert.route(), writtenAlert.route());
        assertEquals(newAlert.score(), writtenAlert.score());
        assertEquals(newAlert.timestamp(), writtenAlert.timestamp());
        assertEquals(newAlert.details(), writtenAlert.details());
        testContext.completeNow();
      })
    ));
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}
