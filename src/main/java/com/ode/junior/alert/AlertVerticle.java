package com.ode.junior.alert;

import com.ode.junior.utils.BaseVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import static com.ode.junior.utils.EventMessageType.ALERT;

public class AlertVerticle extends BaseVerticle {

  private static final Logger log = LoggerFactory.getLogger(AlertVerticle.class);

  private AsyncFile alertFile;

  @Override
  protected void startWithConfiguration(final Promise<Void> startPromise) {
      String filePath = configuration.alertFile();
      vertx.fileSystem().exists(filePath)
        .compose(exists -> {
          final Future<?> future;
          if(exists) {
            log.debug("Alert file " + alertFile + " already exists");
            future = Future.succeededFuture();
          } else {
            log.info("Creating file " + alertFile);
            future = vertx.fileSystem().createFile(filePath)
              .onSuccess(v -> log.info(alertFile + " created"))
              .onFailure(error -> log.error("Could not create file " + alertFile));
          }
          return future;
      })
      .compose(value -> vertx.fileSystem().open(filePath, new OpenOptions().setAppend(true)))
      .onSuccess(openFileResult -> {
        this.alertFile = openFileResult;
        eventBus.consumer(ALERT, this::logAlert);
        startPromise.complete();
      }).onFailure(startPromise::fail);
  }

  private void logAlert(Message<JsonObject> message) {
    alertFile.write(Buffer.buffer(message.body().toString()));
    alertFile.write(Buffer.buffer("\n"));
  }

}
