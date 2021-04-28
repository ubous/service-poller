package coding.is.fun.servicepollerbackend.store;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.model.ServiceStatus;
import io.vertx.core.Future;
import java.util.List;
import java.util.UUID;

public interface ServiceStore {
  Future<Service> add(Service service);
  Future<Service> get(UUID id);
  Future<List<Service>> getAll();
  Future<Service> updateStatus(UUID serviceId, ServiceStatus status);
}
