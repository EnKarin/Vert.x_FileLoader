package ru.cft.fileservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import ru.cft.fileservice.verticle.FileDownloadVerticle;
import ru.cft.fileservice.verticle.FileUploadVerticle;

public class Server extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new Server());
    vertx.deployVerticle(new FileUploadVerticle());
    vertx.deployVerticle(new FileDownloadVerticle());
  }

  @Override
  public void start(Promise<Void> future) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/status").handler(this::handleStatus);
    router.post("/download").handler(this::handleDownload);

    vertx.createHttpServer().requestHandler(router)
      .listen(8080, result -> {
        if (result.succeeded()) {
          future.complete();
        } else {
          future.fail(result.cause());
        }
      });
  }

  private void handleStatus(RoutingContext routingContext) {
    JsonObject json = new JsonObject()
      .put("message", "Application running");
    routingContext.response()
      .putHeader("content-type", "application/json")
      .end(json.encodePrettily());
  }

  private void handleDownload(RoutingContext routingContext) {
    JsonObject data = routingContext.body()
      .asJsonObject();
    String url = data.getString("url");
    String fileName = data.getString("filename");

    vertx.eventBus().send("file-download", JsonObject.of("url", url, "filename", fileName));

    vertx.eventBus().consumer("download-result", result -> {
      JsonObject json = new JsonObject()
        .put("message", result.body());
      routingContext.response()
        .putHeader("content-type", "application/json")
        .end(json.encodePrettily());
    });
  }
}
