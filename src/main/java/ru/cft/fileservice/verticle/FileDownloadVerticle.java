package ru.cft.fileservice.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class FileDownloadVerticle extends AbstractVerticle {

  @Override
  public void start() {
    vertx.eventBus().consumer("file-download", this::handleDownloadEvent);
  }

  private void handleDownloadEvent(Message<JsonObject> message) {
    JsonObject fileRequest = message.body();
    String fileUrl = fileRequest.getString("url");

    WebClientOptions webClientOptions = new WebClientOptions();
    WebClient webClient = WebClient.create(vertx, webClientOptions);
    HttpRequest<Buffer> request = webClient.getAbs(fileUrl);
    request.send(response -> {
      if (response.succeeded()) {
        HttpResponse<Buffer> httpResponse = response.result();
        JsonObject dto = JsonObject.of("buffer", httpResponse.bodyAsBuffer(), "filename", fileRequest.getString("filename"));
        vertx.eventBus().send("file-save", dto);
      } else {
        vertx.eventBus().send("download-result", response.cause().getMessage());
      }
    });
  }
}
