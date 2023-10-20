package ru.cft.fileservice.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class FileUploadVerticle extends AbstractVerticle {

  private String savePath;

  @Override
  public void start() {
    vertx.eventBus().consumer("file-save", this::handleUploadEvent);

    ConfigRetriever.create(vertx)
      .getConfig(config -> {
      if (config.succeeded()) {
        JsonObject configData = config.result();
        savePath = configData.getString("savePath");
      } else {
        // Обработка ошибки получения конфигурационных данных
        System.err.println("Failed to retrieve configuration: " + config.cause().getMessage());
      }
    });
  }

  private void handleUploadEvent(final Message<JsonObject> message) {
    final JsonObject fileDto = message.body();
    final Buffer fileContent = fileDto.getBuffer("buffer");

    vertx.fileSystem().writeFile(savePath + fileDto.getString("filename"), fileContent, result -> {
      if (result.succeeded()) {
        vertx.eventBus().send("download-result", "success");
      } else {
        String resultMessage = result.cause().getMessage();
        vertx.eventBus().send("download-result", resultMessage);
      }
    });
  }
}
