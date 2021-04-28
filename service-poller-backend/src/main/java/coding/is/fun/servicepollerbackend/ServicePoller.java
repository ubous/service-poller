package coding.is.fun.servicepollerbackend;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.model.ServiceStatus;
import coding.is.fun.servicepollerbackend.store.ServiceStore;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;
import java.net.MalformedURLException;
import java.net.URL;

public class ServicePoller {

  private static final Logger LOG = LoggerFactory.getLogger(LoggerHandlerImpl.class);
  public static final int HTTP_DEFAULT_PORT = 80;

  private final ServiceStore store;
  private final HttpClient httpClient;

  private ServicePoller(ServiceStore store, HttpClient httpClient) {
    this.store = store;
    this.httpClient = httpClient;
  }

  public static ServicePoller create(ServiceStore store, HttpClient httpClient) {
    return new ServicePoller(store, httpClient);
  }

  public void pollAll(Long timerId) {
    LOG.info("Polling services");
    store.getAll()
        .onSuccess(services -> {
          LOG.info("Number of services being polled: " + services.size());
          services.forEach(this::pollServiceAndUpdateStatus);
        });
  }

  private void pollServiceAndUpdateStatus(Service service) {
    LOG.info("Checking status for service " + service.getName());
    getStatus(service.getUrl())
        .onSuccess(status -> {
          LOG.info("Status for service " + service.getName() + " is " + status);
          store.updateStatus(service.getId(), status);
        })
        .onFailure(t -> {
          LOG.info("Failed to check status for service " + service.getName() + ". " + t.getMessage());
        })
    ;
  }

  private Future<ServiceStatus> getStatus(String serviceUrl) {
    URL url;
    try {
      url = new URL(serviceUrl);
    } catch (MalformedURLException e) {
      return Future.failedFuture(e);
    }
    var port = url.getPort() != -1 ? url.getPort() : HTTP_DEFAULT_PORT;

    return Future.future(handler -> httpClient
        .request(HttpMethod.GET, port, url.getHost(), url.getPath())
        .compose(HttpClientRequest::send)
        .onSuccess(r -> {
          if (r.statusCode() == 200) {
            handler.complete(ServiceStatus.OK);
            return;
          }
          handler.complete(ServiceStatus.FAIL);
        })
        .onFailure(handler::fail)
    );
  }
}
