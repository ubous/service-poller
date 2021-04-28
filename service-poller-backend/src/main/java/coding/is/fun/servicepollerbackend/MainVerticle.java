package coding.is.fun.servicepollerbackend;

import coding.is.fun.servicepollerbackend.store.InMemoryServiceStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;

public class MainVerticle extends AbstractVerticle {

  public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
  public static final int HTTP_PORT = 8888;

  private static final Logger LOG = LoggerFactory.getLogger(LoggerHandlerImpl.class);
  private static final long POLLER_INTERVAL_IN_MILLISECONDS = 10 * 1000;

  // curl localhost:8888/api/services | jq .
  // curl -X POST -H "Content-Type: application/json" -d '{ "name": "test1", "url": "http://www.google.com"}'  localhost:8888/api/services

  @Override
  public void start(Promise<Void> startPromise) {
    var serviceStore = InMemoryServiceStore.create();
    var httpClient = vertx.createHttpClient();
    var servicePoller = ServicePoller.create(serviceStore, httpClient);
    var requestHandler = RequestHandler.create(serviceStore);
    var router = createRouter(vertx, requestHandler);

    var httpServer = vertx.createHttpServer();
    httpServer.requestHandler(router);
    httpServer.listen(HTTP_PORT, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LOG.info("HTTP server started on port " + HTTP_PORT);
      } else {
        startPromise.fail(http.cause());
      }
    });

    vertx.setPeriodic(POLLER_INTERVAL_IN_MILLISECONDS, servicePoller::pollAll);
  }

  private Router createRouter(Vertx vertx,
                              RequestHandler requestHandler) {
    var router = Router.router(vertx);

    router.route()
        .handler(LoggerHandler.create())
        .handler(BodyHandler.create())
        .failureHandler(ctx -> {
          LOG.error(ctx.failure());
          ctx.next();
        });

    router.route("/api/*").handler(ResponseContentTypeHandler.create());

    router.get("/api/services")
        .produces(CONTENT_TYPE_APPLICATION_JSON)
        .handler(requestHandler::getAllServices);

    router.get("/api/services/:serviceId")
        .produces(CONTENT_TYPE_APPLICATION_JSON)
        .handler(requestHandler::getService);

    router.post("/api/services")
        .consumes(CONTENT_TYPE_APPLICATION_JSON)
        .produces(CONTENT_TYPE_APPLICATION_JSON)
        .handler(requestHandler::addService);

    return router;
  }


}
