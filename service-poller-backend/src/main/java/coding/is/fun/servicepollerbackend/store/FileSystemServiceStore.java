package coding.is.fun.servicepollerbackend.store;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.model.ServiceStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.impl.LoggerHandlerImpl;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileSystemServiceStore implements ServiceStore {

  private static final String SERVICES_FILENAME = "servicesFile.json";
  private static final Logger LOG = LoggerFactory.getLogger(LoggerHandlerImpl.class);

  private final FileSystem fileSystem;

  private FileSystemServiceStore(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    ensureServicesFileExists();
  }

  private void ensureServicesFileExists() {
    fileSystem.exists(SERVICES_FILENAME)
        .onSuccess(exists -> {
          if (exists) {
            LOG.info("Services file already exists");
            return;
          }
          fileSystem.createFile(SERVICES_FILENAME)
              .onSuccess(created -> {
                LOG.info("Services file created: " + SERVICES_FILENAME);
                var emptyJsonArray = new JsonArray();
                rewriteAllToServicesFile(emptyJsonArray.toBuffer());
              })
              .onFailure(t -> LOG.error("Failed to create a services file", t));
        })
        .onFailure(t -> LOG.error("Failed to check if a services file exist", t));
    ;
  }

  public static FileSystemServiceStore create(FileSystem fileSystem) {
    return new FileSystemServiceStore(fileSystem);
  }

  @Override
  public Future<Service> add(Service service) {
    return Future.future(future ->
        getAll()
            .onSuccess(services -> {
              var newServiceList = new ArrayList<>(services);
              newServiceList.add(service);
              var data = Json.encodePrettily(newServiceList);
              rewriteAllToServicesFile(Buffer.buffer(data))
                  .onSuccess(write -> future.complete(service));
            })
    );
  }

  @Override
  public Future<Service> get(UUID id) {
    return Future.future(future ->
        getAll()
            .onSuccess(services -> {
              var serviceOptional = services
                  .stream()
                  .filter(s -> s.getId().equals(id))
                  .findFirst();
              if (serviceOptional.isPresent()) {
                future.complete(serviceOptional.get());
                return;
              }
              future.fail("Could not find service with id " + id);
            }));
  }

  @Override
  public Future<List<Service>> getAll() {
    return Future.future(future ->
        fileSystem
            .readFile(SERVICES_FILENAME)
            .onSuccess(buffer -> {
              var services = buffer
                  .toJsonArray()
                  .stream()
                  .map(object -> (JsonObject) object)
                  .map(convertJsonObjectToService())
                  .collect(Collectors.toList());
              future.complete(services);
            }));
  }

  private Function<JsonObject, Service> convertJsonObjectToService() {
    return jsonObject -> new Service(
        UUID.fromString(jsonObject.getString("id")),
        jsonObject.getString("name"),
        jsonObject.getString("url"),
        Instant.parse(jsonObject.getString("creationTime")),
        Instant.parse(jsonObject.getString("statusUpdateTime")),
        ServiceStatus.valueOf(jsonObject.getString("status"))
    );
  }

  @Override
  public Future<Service> updateStatus(UUID serviceId, ServiceStatus status) {
    var getFuture = get(serviceId);
    var getAllFuture = getAll();
    return Future.future(future ->
        CompositeFuture.all(getFuture, getAllFuture)
            .onSuccess(compositeResult -> {
              var service = (Service) compositeResult.resultAt(0);
              var updatedService = Service.create(service, Instant.now(), status);

              var allServices = (List<Service>)compositeResult.resultAt(1);
              var newServiceList = allServices
                  .stream()
                  .filter(s -> !s.getId().equals(serviceId))
                  .collect(Collectors.toList());

              newServiceList.add(updatedService);

              var data = Json.encodePrettily(newServiceList);

              rewriteAllToServicesFile(Buffer.buffer(data))
                  .onSuccess(write -> future.complete(service));
            })
    );
  }

  private Future<Void> rewriteAllToServicesFile(Buffer buffer) {
    fileSystem.delete(SERVICES_FILENAME);
    return fileSystem.writeFile(SERVICES_FILENAME, buffer);
  }
}
