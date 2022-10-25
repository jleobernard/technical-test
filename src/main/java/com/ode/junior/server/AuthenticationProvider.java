package com.ode.junior.server;

import com.ode.junior.configuration.Configuration;
import com.ode.junior.configuration.PropFileUserCredential;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AuthenticationProvider implements io.vertx.ext.auth.authentication.AuthenticationProvider  {
  private final Map<String, String> store;

  public AuthenticationProvider(Configuration configuration) {
    this.store = configuration.userCredentials().stream().collect(toMap(
      PropFileUserCredential::email, PropFileUserCredential::password
    ));
  }

  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> resultHandler) {
    System.out.println("Coucou");
  }

  @Override
  public void authenticate(Credentials credentials, Handler<AsyncResult<User>> handler) {
    if(credentials instanceof final UsernamePasswordCredentials upCredentials) {
      final String username = upCredentials.getUsername();
      final String password = upCredentials.getPassword();
      if (username.isBlank()) {
        handler.handle(Future.failedFuture("username.mandatory"));
      } else if (!this.store.containsKey(username)) {
        handler.handle(Future.failedFuture("username.does.not.exist"));
      } else if (this.store.get(username).equals(password)) {
        handler.handle(Future.succeededFuture(User.fromName(username)));
      } else {
        handler.handle(Future.failedFuture("password.mismatch"));
      }
    }
  }
}
