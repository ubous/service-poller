package coding.is.fun.servicepollerbackend.model;

import java.time.Instant;
import java.util.UUID;

public class Service {

  private final UUID id;
  private final String name;
  private final String url;
  private final Instant creationTime;

  private Service(UUID id, String name, String url, Instant creationTime) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.creationTime = creationTime;
  }

  public static Service create(UUID id, String name, String url, Instant creationTime) {
    return new Service(id, name, url, creationTime);
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
}
