package com.ode.junior.checker;

import com.ode.junior.configuration.ComparatorWeight;
import com.ode.junior.configuration.ConnectionInformationType;
import com.ode.junior.parser.UserConnection;
import com.ode.junior.parser.UserConnectionForRoute;
import com.ode.junior.utils.BaseVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ode.junior.utils.EventMessageType.*;
import static com.ode.junior.utils.VertxUtils.getMessageBodyAs;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

public class UserConnectionCheckerVerticle extends BaseVerticle {
  private static final Logger log = LoggerFactory.getLogger(UserConnectionCheckerVerticle.class);

  private int threshold;

  private Map<ConnectionInformationType, Integer> weights;

  @Override
  public void startWithConfiguration(final Promise<Void> startPromise) {
      this.threshold = configuration.threshold();
      this.weights = configuration.weights().stream().collect(toMap(ComparatorWeight::type, ComparatorWeight::weight));
      this.eventBus.consumer(USER_CONNECTION_AVAILABLE, this::check);
      startPromise.complete();
  }

  private void check(Message<?> message) {
    final UserConnectionForRoute userConnectionForRoute = ((JsonObject) message.body()).mapTo(UserConnectionForRoute.class);
    final UserConnection userConnection = userConnectionForRoute.userConnection();
    final String userPrincipal = userConnection.user();
    log.debug("Get " + userPrincipal + "'s connection details");
    this.eventBus.request(GET_CONNECTION_DETAILS, userPrincipal).onSuccess(storedConnection -> {
      final UserConnectionCheckResult score;
      if (storedConnection.body() == null) {
        log.debug("This is " + userPrincipal + "'s first connection");
        score = new UserConnectionCheckResult(0, emptyList());
        send(UPDATE_CONNECTION_DETAILS, userConnection);
      } else {
        log.debug("Got " + userPrincipal + "'s connection details");
        score = compareConnections(getMessageBodyAs(storedConnection, UserConnection.class), userConnection);
      }
      log.debug(userPrincipal + " score is " + score);
      if (score.score() >= threshold) {
        log.debug("Sending an alert event for " + userPrincipal + "'s current connection");
        send(ALERT, new Alert(userPrincipal, userConnectionForRoute.route(), score.score(),
          score.differences(), System.currentTimeMillis()));
      } else {
        log.debug("No alert to send for " + userPrincipal + "'s current connection");
      }
    });
  }

  private UserConnectionCheckResult compareConnections(final UserConnection storedConnection, final UserConnection currentConnection) {
    final List<UserConnectionDifference> differences = new ArrayList<>();
    int score = 0;
    for (ConnectionInformationType informationType : ConnectionInformationType.values()) {
      final String storedDetail = storedConnection.details().getOrDefault(informationType, "");
      final String currentDetail = currentConnection.details().getOrDefault(informationType, "");
      if(!storedDetail.equalsIgnoreCase(currentDetail)) {
        score += this.weights.getOrDefault(informationType, 0);
        differences.add(new UserConnectionDifference(informationType.getCode(), storedDetail, currentDetail));
      }
    }
    return new UserConnectionCheckResult(score, differences);
  }
}
