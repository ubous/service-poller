package coding.is.fun.servicepollerbackend.model;

import java.time.Instant;
import java.util.UUID;

public class Service {

  private final UUID id;
  private final String name;
  private final String url;
  private final Instant creationTime;
  private final Instant statusUpdateTime;
  private final ServiceStatus status;

  private Service(UUID id, String name, String url, Instant creationTime,
                  Instant statusUpdateTime,
                  ServiceStatus status) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.creationTime = creationTime;
    this.statusUpdateTime = statusUpdateTime;
    this.status = status;
  }

  public static Service create(UUID id, String name, String url, Instant creationTime) {
    return new Service(id, name, url, creationTime, null, ServiceStatus.UNKNOWN);
  }

  public static Service create(Service service, Instant statusUpdateTime, ServiceStatus status) {
    return new Service(
        service.getId(),
        service.getName(),
        service.getUrl(),
        service.getCreationTime(),
        statusUpdateTime,
        status
    );
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public Instant getStatusUpdateTime() {
    return statusUpdateTime;
  }

  public ServiceStatus getStatus() {
    return status;
  }
}
