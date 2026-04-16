package ee.aleksale.releaseapp.service.external;


import ee.aleksale.releaseapp.config.GitlabConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GitlabApiService {

  private static final String TOKEN_PREFIX = "Bearer ";

  private final GitlabConfig gitlabConfig;
  private final WebClient webClient;

  public GitlabApiService(GitlabConfig config, WebClient.Builder webClientBuilder) {
    this.gitlabConfig = config;
    this.webClient = webClientBuilder.build();
  }

  private WebClient.RequestHeadersSpec<?> get(String uri) {
    return webClient.get()
            .uri(gitlabConfig.getBaseUrl() + "/api/v4" + uri)
            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + gitlabConfig.getToken());
  }

  private WebClient.RequestBodySpec post(String uri) {
    return webClient.post()
            .uri(gitlabConfig.getBaseUrl() + "/api/v4" + uri)
            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + gitlabConfig.getToken());
  }
}
