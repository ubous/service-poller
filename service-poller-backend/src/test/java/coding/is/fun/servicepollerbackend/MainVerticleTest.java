package coding.is.fun.servicepollerbackend;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

  private final int HTTP_PORT = 8888;
  private final String HTTP_SERVER = "localhost";

  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeedingThenComplete());
  }

  @Test
  void verticleShouldBeDeployed(Vertx vertx, VertxTestContext testContext) {
    testContext.completeNow();
  }

  @Test
  void getAllServicesWithNoServices(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpClient()
        .request(HttpMethod.GET, HTTP_PORT, HTTP_SERVER, "/api/services")
        .compose(HttpClientRequest::send)
        .onComplete(testContext.succeeding(
            response -> testContext.verify(() -> assertThat(response.statusCode()).isEqualTo(200))))
        .compose(HttpClientResponse::body)
        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
          assertThat(buffer.toString()).isEqualTo("[]");
          testContext.completeNow();
        })));
  }

  @Test
  void getServiceThatDoesNotExist(Vertx vertx, VertxTestContext testContext) {
    var id = UUID.randomUUID();

    vertx.createHttpClient()
        .request(HttpMethod.GET, HTTP_PORT, HTTP_SERVER, "/api/services/" + id)
        .compose(HttpClientRequest::send)
        .onComplete(testContext.succeeding(
            response -> testContext.verify(() -> assertThat(response.statusCode()).isEqualTo(404))))
        .compose(HttpClientResponse::body)
        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
          assertThat(buffer.toString()).isEqualTo("Not Found");
          testContext.completeNow();
        })));
  }

  @Test
  void getServiceWithInvalidUuid(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpClient()
        .request(HttpMethod.GET, HTTP_PORT, HTTP_SERVER, "/api/services/1")
        .compose(HttpClientRequest::send)
        .onComplete(testContext.succeeding(
            response -> {
              testContext.verify(() -> assertThat(response.statusCode()).isEqualTo(400));
              testContext.completeNow();
            }
        ));
  }

  @Test
  void addService(Vertx vertx, VertxTestContext testContext) {
    var serviceJson =
        "{" +
        "\"name\":\"test1\"," +
        "\"url\":\"http://test1.org\"" +
        "}";

    var client = vertx.createHttpClient();

    sendAddServiceRequest(client, serviceJson)
        .compose(HttpClientResponse::body)
        .map(Json::decodeValue)
        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
          assertThat(buffer).isInstanceOf(JsonObject.class);
          var json = (JsonObject) buffer;
          assertThat(json.getString("name")).isEqualTo("test1");
          assertThat(json.getString("url")).isEqualTo("http://test1.org");
          testContext.completeNow();
        })));
  }

  @Test
  void addTwoServicesThenGetAllServices(Vertx vertx, VertxTestContext testContext) {
    var client = vertx.createHttpClient();
    var serviceBody1 =
        "{" +
        "\"name\":\"test1\"," +
        "\"url\":\"http://test1.org\"" +
        "}";
    var serviceBody2 =
        "{" +
        " \"name\":\"test2\"," +
        " \"url\":\"http://test2.org\"" +
        "}";

    var service1Future = sendAddServiceRequest(client, serviceBody1);
    var service2Future = sendAddServiceRequest(client, serviceBody2);

    CompositeFuture.all(service1Future, service2Future)
        .onFailure(ignored -> testContext.failNow("Failed to create test services"))
        .onSuccess(ignored -> client
            .request(HttpMethod.GET, HTTP_PORT, HTTP_SERVER, "/api/services")
            .compose(HttpClientRequest::send)
            .compose(HttpClientResponse::body)
            .map(Json::decodeValue)
            .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
              assertThat(buffer).isInstanceOf(JsonArray.class);
              var json = (JsonArray) buffer;
              assertThat(json.getList()).hasSize(2);
              testContext.completeNow();
            })))
        );
  }

  private Future<HttpClientResponse> sendAddServiceRequest(HttpClient client, String serviceJson) {
    return client
        .request(HttpMethod.POST, HTTP_PORT, HTTP_SERVER, "/api/services")
        .compose(
            request -> request.putHeader("content-type", "application/json").send(serviceJson));
  }
}
