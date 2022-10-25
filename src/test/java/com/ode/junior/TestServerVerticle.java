package com.ode.junior;

import com.ode.junior.server.ServerVerticle;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static com.ode.junior.utils.EventMessageType.INCOMING_REQUEST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestServerVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new ServerVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void can_access_public_resources(Vertx vertx, VertxTestContext testContext) {
    final HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8001, "localhost", "/public/foo")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
        final JsonObject jsonResponse = buffer.toJsonObject();
        assertTrue(jsonResponse.getBoolean("success"));
        assertEquals("Welcome to /public/foo", jsonResponse.getString("message"));
        testContext.completeNow();
      })));
  }

  @Test
  void cannot_access_protected_resources_if_not_authenticated(Vertx vertx, VertxTestContext testContext) {
    final HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8001, "localhost", "/protected/foo")
      .compose(HttpClientRequest::send)
      .onComplete(testContext.succeeding(response -> testContext.verify(() -> {
        assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
        testContext.completeNow();
      })));
  }

  @Test
  void can_access_protected_resources_if_authenticated(Vertx vertx, VertxTestContext testContext) {
    final WebClient client = WebClient.create(vertx);
    client.get(8001, "localhost", "/protected/foo")
      .putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("account@mail.com:password2".getBytes(UTF_8)))
      .send()
      .map(HttpResponse::statusCode)
      .onComplete(testContext.succeeding(responseCode -> testContext.verify(() -> {
        assertEquals(HttpResponseStatus.OK.code(), responseCode);
        testContext.completeNow();
      })));
  }

  @Test
  void trigger_send_bus_on_access_protected_ressource(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx.eventBus().consumer(INCOMING_REQUEST, message -> testContext.verify(() -> {
      JsonObject payload = (JsonObject)message.body();
      assertEquals("127.0.0.1", payload.getString("ip"));
      assertEquals("/protected/bar", payload.getString("route"));
      assertEquals("Vert.x-WebClient/4.3.4", payload.getString("userAgent"));
      assertEquals("account@mail.com", payload.getString("user"));
      testContext.completeNow();
    }));
    final WebClient client = WebClient.create(vertx);
    client.get(8001, "localhost", "/protected/bar")
      .putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString("account@mail.com:password2".getBytes(UTF_8)))
      .send();
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}
