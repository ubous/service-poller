package coding.is.fun.servicepollerbackend.store;

import static org.assertj.core.api.Assertions.assertThat;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.model.ServiceStatus;
import io.vertx.core.CompositeFuture;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryServiceStoreTest {

  private final InMemoryServiceStore store = InMemoryServiceStore.create();

  @Test
  void shouldGetServiceThatWasPreviouslyAdded() {
    // given
    var id = UUID.randomUUID();
    var service = Service.create(id, "test", "http://test.org", Instant.now());
    store.add(service)
        // when
        .compose(s -> store.get(id))
        .onComplete(handler -> {
          // then
          assertThat(handler.succeeded()).isTrue();
          assertThat(handler.result()).isEqualTo(service);
        });
  }

  @Test
  void shouldReturnFailedFutureIfServiceCannotBeFound() {
    // given
    var id = UUID.randomUUID();

    // when
    store.get(id)
        .onComplete(handler -> {
          // then
          assertThat(handler.failed()).isTrue();
          assertThat(handler.cause()).isInstanceOf(ServiceNotFoundException.class);
        });
  }

  @Test
  void shouldReturnAllPreviouslyAddedServices() {
    // given
    var service1 = Service.create(UUID.randomUUID(), "test1", "http://test1.org", Instant.now());
    var service2 = Service.create(UUID.randomUUID(), "test2", "http://test2.org", Instant.now());
    var service3 = Service.create(UUID.randomUUID(), "test3", "http://test3.org", Instant.now());

    var addService1Future = store.add(service1);
    var addService2Future = store.add(service2);
    var addService3Future = store.add(service3);

    CompositeFuture.all(addService1Future, addService2Future, addService3Future)
        // when
        .compose(compositeResult -> store.getAll())
        .onComplete(handler -> {
          // then
          assertThat(handler.succeeded()).isTrue();
          var result = handler.result();
          assertThat(result).hasSize(3);
          assertThat(result).contains(service1, service2, service3);
        });
  }

  @Test
  void shouldUpdateServiceStatusForAServiceThatExists() {
    // given
    var id = UUID.randomUUID();
    var service = Service.create(id, "test", "http://test.org", Instant.now());
    store.add(service)
        // when
        .compose(s -> store.updateStatus(s.getId(), ServiceStatus.OK))
        .onComplete(handler -> {
          // then
          assertThat(handler.succeeded()).isTrue();
          assertThat(handler.result().getStatus()).isEqualTo(ServiceStatus.OK);
          assertThat(handler.result().getStatusUpdateTime()).isNotNull();

          assertThat(handler.result().getName()).isEqualTo("test");
          assertThat(handler.result().getUrl()).isEqualTo("http://test.org");
          assertThat(handler.result().getStatus()).isNotEqualTo(service.getStatus());
          assertThat(handler.result().getStatusUpdateTime()).isNotEqualTo(service.getStatusUpdateTime());
        });
  }
}
