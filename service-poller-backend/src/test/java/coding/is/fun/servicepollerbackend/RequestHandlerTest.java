package coding.is.fun.servicepollerbackend;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import coding.is.fun.servicepollerbackend.model.Service;
import coding.is.fun.servicepollerbackend.store.ServiceNotFoundException;
import coding.is.fun.servicepollerbackend.store.ServiceStore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestHandlerTest {

  @Mock
  private ServiceStore store;

  @Mock
  private RoutingContext routingContext;

  private RequestHandler requestHandler;

  @BeforeEach
  void beforeEach() {
    requestHandler = RequestHandler.create(store);
  }

  @Test
  void getAllServicesShouldRetrieveAllFromStoreAndConvertToJson() {
    // given
    var mockedServices = List.of(mock(Service.class));
    when(store.getAll()).thenReturn(succeededFuture(mockedServices));

    // when
    requestHandler.getAllServices(routingContext);

    verify(store).getAll();
    verify(routingContext).json(mockedServices);
  }

  @Test
  void getServiceShouldReturnServiceIfPresentInTheStoreAndConvertToJson() {
    // given
    var id = UUID.randomUUID();
    var serviceMock = mock(Service.class);
    when(store.get(id)).thenReturn(succeededFuture(serviceMock));

    when(routingContext.pathParam("serviceId")).thenReturn(id.toString());

    // when
    requestHandler.getService(routingContext);

    verify(store).get(id);
    verify(routingContext).json(serviceMock);
  }

  @Test
  void getServiceShouldFailIfServiceIsNotPresentInTheStore() {
    // given
    var id = UUID.randomUUID();
    var error = new ServiceNotFoundException("Service not found");
    when(store.get(id)).thenReturn(failedFuture(error));

    when(routingContext.pathParam("serviceId")).thenReturn(id.toString());

    // when
    requestHandler.getService(routingContext);

    verify(store).get(id);
    verify(routingContext).fail(404, error);
  }

  @Test
  void getServiceShouldFailIfIdIsNotUuid() {
    // given
    when(routingContext.pathParam("serviceId")).thenReturn("11");

    // when
    requestHandler.getService(routingContext);

    verifyNoMoreInteractions(store);
    verify(routingContext).fail(eq(400), any(IllegalArgumentException.class));
  }

  @Test
  void addServiceShouldAddToStoreAndReturnTheServiceAsJson() {
    // given
    when(routingContext.getBodyAsJson()).thenReturn(new JsonObject(
        "{" +
          "\"name\":\"test\"," +
          "\"url\":\"http://test.org\"" +
        "}"
        ));

    var serviceMock = mock(Service.class);
    when(store.add(any(Service.class))).thenReturn(succeededFuture(serviceMock));

    // when
    requestHandler.addService(routingContext);

    // then
    ArgumentCaptor<Service> serviceArgCaptor = ArgumentCaptor.forClass(Service.class);
    verify(store).add(serviceArgCaptor.capture());
    assertThat(serviceArgCaptor.getValue().getName()).isEqualTo("test");
    assertThat(serviceArgCaptor.getValue().getUrl()).isEqualTo("http://test.org");

    verify(routingContext).json(serviceMock);
  }

  @ParameterizedTest(name = "requestBody={0}")
  @ValueSource(strings = {"{ \"name\":\"test\" }", "{ \"url\":\"http://test.org\" }"})
  void addServiceShouldFailIfRequireFieldIsNotPresent(String requestBody) {
    // given
    when(routingContext.getBodyAsJson()).thenReturn(new JsonObject(requestBody));

    // when
    requestHandler.addService(routingContext);

    // then
    verifyNoMoreInteractions(store);
    verify(routingContext).fail(eq(400), any(IllegalArgumentException.class));
  }

  @Test
  void addServiceShouldFailIfTheUrlIsNotAValidUri() {
    // given
    var requestBody = "{" +
                      "\"name\":\"test\"," +
                      "\"url\":\"not-a-valid-uri\"" +
                      "}";
    when(routingContext.getBodyAsJson()).thenReturn(new JsonObject(requestBody));

    // when
    requestHandler.addService(routingContext);

    // then
    verifyNoMoreInteractions(store);
    verify(routingContext).fail(eq(400), any(MalformedURLException.class));
  }
}
