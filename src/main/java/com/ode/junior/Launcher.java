package com.ode.junior;

import com.ode.junior.alert.AlertVerticle;
import com.ode.junior.checker.UserConnectionCheckerVerticle;
import com.ode.junior.parser.ConnectionParserVerticle;
import com.ode.junior.server.ServerVerticle;
import com.ode.junior.userstore.UserConnectionStoreVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

public class Launcher {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    // set vertx logger delegate factory to slf4j
    String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
    if (logFactory == null) {
      System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    }
    vertx.deployVerticle(new ServerVerticle());
    vertx.deployVerticle(new ConnectionParserVerticle());
    vertx.deployVerticle(new UserConnectionCheckerVerticle());
    vertx.deployVerticle(new UserConnectionStoreVerticle());
    vertx.deployVerticle(new AlertVerticle());
  }
}
