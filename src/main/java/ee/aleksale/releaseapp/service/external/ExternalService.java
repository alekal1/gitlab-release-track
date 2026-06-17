package ee.aleksale.releaseapp.service.external;

import org.springframework.web.reactive.function.client.WebClient;

public abstract class ExternalService {

  protected static final String TOKEN_PREFIX = "Bearer ";
  protected final WebClient webClient;

  public ExternalService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  protected abstract WebClient.RequestHeadersSpec<?> get(String uri);
}
