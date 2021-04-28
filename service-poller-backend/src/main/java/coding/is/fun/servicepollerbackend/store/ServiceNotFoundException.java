package coding.is.fun.servicepollerbackend.store;

public class ServiceNotFoundException extends RuntimeException {

  public ServiceNotFoundException(String message) {
    super(message);
  }
}
