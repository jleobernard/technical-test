package com.ode.junior.parser;

import com.ode.junior.configuration.ConnectionInformationType;
import com.ode.junior.server.IncomingRequestDetails;
import com.ode.junior.utils.BaseVerticle;
import eu.bitwalker.useragentutils.UserAgent;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.HashMap;
import java.util.Map;

import static com.ode.junior.utils.EventMessageType.INCOMING_REQUEST;
import static com.ode.junior.utils.EventMessageType.USER_CONNECTION_AVAILABLE;

public class ConnectionParserVerticle extends BaseVerticle {
  private static final Logger log = LoggerFactory.getLogger(ConnectionParserVerticle.class);

  @Override
  protected void startWithConfiguration(Promise<Void> startPromise) {
      eventBus.consumer(INCOMING_REQUEST, this::parse);
      startPromise.complete();
  }

  private void parse(Message<JsonObject> message) {
    final IncomingRequestDetails details = message.body().mapTo(IncomingRequestDetails.class);
    final Map<ConnectionInformationType, String> parsedDetails = new HashMap<>();
    parsedDetails.put(ConnectionInformationType.IP, details.ip());
    parsedDetails.putAll(parseUserAgent(details.userAgent()));
    final UserConnection userConnection = new UserConnection(details.user(), parsedDetails);
    final UserConnectionForRoute userConnectionForRoute = new UserConnectionForRoute(userConnection, details.route());
    send(USER_CONNECTION_AVAILABLE, userConnectionForRoute);
  }

  private Map<ConnectionInformationType, String> parseUserAgent(String userAgent) {
    final Map<ConnectionInformationType, String> parsedValues;
    if (userAgent == null || userAgent.isBlank()) {
      parsedValues = Map.of(ConnectionInformationType.NOTHING, "true");
    } else {
      final Parser uaParser = new Parser();
      final Client client = uaParser.parse(userAgent);
      final UserAgent userAgentModel = UserAgent.parseUserAgentString(userAgent);
      parsedValues = new HashMap<>();
      parsedValues.put(ConnectionInformationType.DEVICE_TYPE, getDeviceType(userAgentModel));
      parsedValues.put(ConnectionInformationType.DEVICE_BRAND, getDeviceBrand(client));
      parsedValues.put(ConnectionInformationType.OS_NAME, getOsName(client, userAgentModel));
      parsedValues.put(ConnectionInformationType.OS_FAMILY, getOsFamily(client, userAgentModel));
      parsedValues.put(ConnectionInformationType.OS_VERSION, getOsVersion(client, userAgentModel));
      parsedValues.put(ConnectionInformationType.CLIENT_NAME, getClientName(client, userAgentModel));
      parsedValues.put(ConnectionInformationType.CLIENT_TYPE, getClientType(client, userAgentModel));
      parsedValues.put(ConnectionInformationType.CLIENT_VERSION, getClientVersion(client, userAgentModel, userAgent));
    }
    log.debug("Parsed values are " + parsedValues);
    return parsedValues;
  }

  private String getClientVersion(Client client, UserAgent userAgentModel, final String rawUserAgent) {
    return userAgentModel.getBrowser().getVersion(rawUserAgent).getVersion();
  }

  private String getClientType(Client client, UserAgent userAgentModel) {
    return userAgentModel.getBrowser().getBrowserType().getName();
  }

  private String getClientName(Client client, UserAgent userAgentModel) {
    return userAgentModel.getBrowser().getGroup().getName();
  }

  private String getOsVersion(Client client, UserAgent userAgentModel) {
    return client.userAgent.major + "." + client.userAgent.minor;
  }

  private String getOsFamily(Client client, UserAgent userAgentModel) {
    return userAgentModel.getOperatingSystem().getGroup().getName();
  }

  private String getOsName(Client client, UserAgent userAgentModel) {
    return userAgentModel.getOperatingSystem().getName();
  }

  private String getDeviceType(UserAgent client) {
    return client.getOperatingSystem().getDeviceType().getName();
  }
  private String getDeviceBrand(Client client) {
    return client.device.family;
  }
}
