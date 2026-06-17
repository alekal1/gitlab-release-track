package ee.aleksale.releaseapp.service.external;

import ee.aleksale.releaseapp.config.JiraConfig;
import ee.aleksale.releaseapp.model.dto.response.jira.JiraBoardsResponse;
import ee.aleksale.releaseapp.model.dto.response.jira.JiraIssuesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class JiraApiService extends ExternalService {

  private final JiraConfig jiraConfig;

  public JiraApiService(JiraConfig jiraConfig, WebClient.Builder webClientBuilder) {
    super(webClientBuilder);
    this.jiraConfig = jiraConfig;
  }

  @Override
  protected WebClient.RequestHeadersSpec<?> get(String uri) {
    return webClient.get()
            .uri(jiraConfig.getBaseUrl() + "/rest/agile/1.0" + uri)
            .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + jiraConfig.getToken());
  }

  public Mono<JiraBoardsResponse> getBoards() {
    return get("/board")
            .retrieve()
            .bodyToMono(JiraBoardsResponse.class)
            .doOnError(e -> log.error("Failed to search jira boards"));
  }

  public Mono<JiraIssuesResponse> getBoardsIssuesOnColumn() {
    final var allBoardsResponse = getBoards();
    if (allBoardsResponse == null) {
      return Mono.just(new JiraIssuesResponse());
    }

    final var allBoards = allBoardsResponse.block();
    if (allBoards == null || allBoards.getBoards() == null || allBoards.getBoards().isEmpty()) {
      log.error("No board found");
      return Mono.just(new JiraIssuesResponse());
    }

    final var board = allBoards.getBoards().stream()
            .filter(b -> jiraConfig.getBoardName().equals(b.getName()))
            .findFirst();

    if (board.isEmpty()) {
      log.error("No board found");
      return Mono.just(new JiraIssuesResponse());
    }

    final var url = "/board/" + board.get().getId() + "/issue?jql=status%20in%20(\"" + jiraConfig.getIssueStatus().replace(" ", "%20") + "\")";

    return get(url)
            .retrieve()
            .bodyToMono(JiraIssuesResponse.class)
            .doOnError(e -> log.error("Failed to search jira issues", e));
  }
}
