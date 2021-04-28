package coding.is.fun.servicepollerbackend.store;

import coding.is.fun.servicepollerbackend.model.Service;
import io.vertx.core.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryServiceStore implements ServiceStore {

  private final Map<UUID, Service> servicesMap = new ConcurrentHashMap<>();

  @Override
  public Future<Service> add(Service service) {
    servicesMap.put(service.getId(), service);
    return Future.succeededFuture(service);
  }

  @Override
  public Future<Service> get(UUID id) {
    var service = servicesMap.get(id);
    if (service == null) {
      return Future.failedFuture(new ServiceNotFoundException("Could not find service with id " + id.toString()));
    }
    return Future.succeededFuture(service);
  }

  @Override
  public Future<List<Service>> getAll() {
    var all = new ArrayList<>(servicesMap.values());
    return Future.succeededFuture(all);
  }
}
