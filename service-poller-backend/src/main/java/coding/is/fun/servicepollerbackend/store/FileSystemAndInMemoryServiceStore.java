package coding.is.fun.servicepollerbackend.store;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.model.ServiceStatus;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileSystemAndInMemoryServiceStore implements ServiceStore {

  private static final String SERVICES_FILENAME = "servicesFile.json";

  private final FileSystem fileSystem;
  private final InMemoryServiceStore inMemoryStore;

  private FileSystemAndInMemoryServiceStore(FileSystem fileSystem,
                                            InMemoryServiceStore inMemoryStore) {
    this.fileSystem = fileSystem;
    this.inMemoryStore = inMemoryStore;

    fileSystem
        .exists(SERVICES_FILENAME)
        .onSuccess(exists -> {
          if (!exists) {
            return;
          }
          loadFromFileToInMemoryStore();
        });
  }

  public static FileSystemAndInMemoryServiceStore create(FileSystem fileSystem,
                                                         InMemoryServiceStore inMemoryStore) {
    return new FileSystemAndInMemoryServiceStore(fileSystem, inMemoryStore);
  }

  @Override
  public Future<Service> add(Service service) {
    return inMemoryStore.add(service)
        .onSuccess(this::writeAllServicesToFile);
  }

  @Override
  public Future<Service> get(UUID id) {
    return inMemoryStore.get(id);
  }

  @Override
  public Future<List<Service>> getAll() {
    return inMemoryStore.getAll();
  }

  @Override
  public Future<Service> updateStatus(UUID serviceId, ServiceStatus status) {
    return inMemoryStore.updateStatus(serviceId, status)
        .onSuccess(this::writeAllServicesToFile);
  }

  @Override
  public Future<Void> delete(UUID id) {
    return inMemoryStore.delete(id)
        .onSuccess(ignored -> writeAllServicesToFile());
  }

  @Override
  public Future<Service> updateService(UUID id, String name, String url) {
    return inMemoryStore.updateService(id, name, url)
        .onSuccess(this::writeAllServicesToFile);
  }

  private void loadFromFileToInMemoryStore() {
    fileSystem
        .readFile(SERVICES_FILENAME)
        .onSuccess(buffer -> {
              var services = buffer
                  .toJsonArray()
                  .stream()
                  .map(object -> (JsonObject) object)
                  .map(this::hydrateService)
                  .collect(Collectors.toList());
              inMemoryStore.addAll(services);
            }
        );
  }

  private Service hydrateService(JsonObject jsonObject) {
    return new Service(
        UUID.fromString(jsonObject.getString("id")),
        jsonObject.getString("name"),
        jsonObject.getString("url"),
        Instant.parse(jsonObject.getString("creationTime")),
        Instant.parse(jsonObject.getString("statusUpdateTime")),
        ServiceStatus.valueOf(jsonObject.getString("status"))
    );
  }

  private void writeAllServicesToFile() {
    getAll()
        .onSuccess(services -> {
          var json = Json.encodePrettily(services);
          fileSystem.writeFile(SERVICES_FILENAME, Buffer.buffer(json));
        });
  }

  private void writeAllServicesToFile(Service service) {
    writeAllServicesToFile();
  }
}
