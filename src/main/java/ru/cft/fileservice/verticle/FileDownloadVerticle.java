package ru.cft.fileservice.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class FileDownloadVerticle extends AbstractVerticle {

  @Override
  public void start() {
    vertx.eventBus().consumer("file-download", this::handleDownloadEvent);
  }

  private void handleDownloadEvent(final Message<JsonObject> message) {
    final JsonObject fileRequest = message.body();
    final String fileUrl = fileRequest.getString("url");

    final WebClientOptions webClientOptions = new WebClientOptions();
    WebClient.create(vertx, webClientOptions)
      .getAbs(fileUrl)
      .send(response -> {
      if (response.succeeded()) {
        JsonObject dto = JsonObject.of(
          "buffer", response.result().bodyAsBuffer(), "filename",
          fileRequest.getString("filename")
        );
        vertx.eventBus().send("file-save", dto);
      } else {
        vertx.eventBus().send("download-result", response.cause().getMessage());
      }
    });
  }
}
