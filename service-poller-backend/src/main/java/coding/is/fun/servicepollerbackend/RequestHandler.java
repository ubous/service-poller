package coding.is.fun.servicepollerbackend;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.store.ServiceStore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class RequestHandler {

  private static final int HTTP_STATUS_CODE_BAD_REQUEST = 400;
  private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
  private final ServiceStore serviceStore;

  private RequestHandler(ServiceStore serviceStore) {
    this.serviceStore = serviceStore;
  }

  public static RequestHandler create(ServiceStore serviceStore) {
    return new RequestHandler(serviceStore);
  }

  public void getAllServices(RoutingContext routingContext) {
    serviceStore
        .getAll()
        .onSuccess(routingContext::json)
        .onFailure(routingContext::fail);
  }

  public void getService(RoutingContext routingContext) {
    var idParam = routingContext.pathParam("serviceId");
    try {
      var id = UUID.fromString(idParam);

      serviceStore.get(id)
          .onSuccess(routingContext::json)
          .onFailure(throwable -> {
            routingContext.fail(HTTP_STATUS_CODE_NOT_FOUND, throwable);
          });
    } catch (IllegalArgumentException ex) {
      routingContext.fail(HTTP_STATUS_CODE_BAD_REQUEST, ex);
    }
  }

  public void addService(RoutingContext routingContext) {
    var bodyJson = routingContext.getBodyAsJson();

    var name = getName(routingContext, bodyJson);
    if (name == null) {
      return;
    }

    var url = getUrl(routingContext, bodyJson);
    if (url == null) {
      return;
    }

    var id = UUID.randomUUID();
    var service = Service.create(id, name, url, Instant.now());

    serviceStore.add(service)
        .onSuccess(routingContext::json)
        .onFailure(routingContext::fail);
  }

  private String getName(RoutingContext routingContext, JsonObject bodyJson) {
    var name = bodyJson.getString("name");

    if (StringUtils.isEmpty(name)) {
      routingContext.fail(HTTP_STATUS_CODE_BAD_REQUEST,
          new IllegalArgumentException("name is required to add a service"));
      return null;
    }
    return name;
  }

  private String getUrl(RoutingContext routingContext, JsonObject bodyJson) {
    var url = bodyJson.getString("url");

    if (StringUtils.isEmpty(url)) {
      routingContext.fail(HTTP_STATUS_CODE_BAD_REQUEST,
          new IllegalArgumentException("url is required to add a service"));
      return null;
    }

    try {
      new URL(url);
    } catch (MalformedURLException ex) {
      routingContext.fail(HTTP_STATUS_CODE_BAD_REQUEST, ex);
      return null;
    }
    return url;
  }


  public void deleteService(RoutingContext routingContext) {
    var idParam = routingContext.pathParam("serviceId");
    try {
      var id = UUID.fromString(idParam);

      serviceStore.delete(id)
        .onSuccess(ignored -> routingContext.response().end())
        .onFailure(routingContext::fail);
    } catch (IllegalArgumentException ex) {
      routingContext.fail(HTTP_STATUS_CODE_BAD_REQUEST, ex);
    }
  }

  public void updateService(RoutingContext routingContext) {
    var bodyJson = routingContext.getBodyAsJson();

    var idParam = routingContext.pathParam("serviceId");
    UUID id;
    try {
      id = UUID.fromString(idParam);
    } catch (IllegalArgumentException ex) {
      routingContext.fail(HTTP_STATUS_CODE_BAD_REQUEST, ex);
      return;
    }

    var name = getName(routingContext, bodyJson);
    if (name == null) {
      return;
    }

    var url = getUrl(routingContext, bodyJson);
    if (url == null) {
      return;
    }

    serviceStore.updateService(id, name, url)
        .onSuccess(routingContext::json)
        .onFailure(routingContext::fail);
  }
}
