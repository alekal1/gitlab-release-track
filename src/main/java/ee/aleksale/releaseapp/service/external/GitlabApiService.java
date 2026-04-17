package ee.aleksale.releaseapp.service.external;


import ee.aleksale.releaseapp.config.GitlabConfig;
import ee.aleksale.releaseapp.model.dto.response.GitlabFetchTagsResponse;
import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineJobResponse;
import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineResponse;
import ee.aleksale.releaseapp.model.dto.response.GitlabSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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

  public Mono<List<GitlabSearchResponse>> searchProjects(String projectName) {
    return get("/projects?search=" + projectName + "&membership=true&per_page=20&order_by=name")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<GitlabSearchResponse>>() {})
            .doOnError(e -> log.error("Failed to search projects: {}", e.getMessage()));
  }

  public Mono<List<GitlabFetchTagsResponse>> getTags(Long gitlabProjectId) {
    return get("/projects/" + gitlabProjectId + "/repository/tags?order_by=updated&sort=desc&per_page=100")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<GitlabFetchTagsResponse>>() {})
            .doOnError(e -> log.error("Failed to fetch tags for project {}: {}", gitlabProjectId, e.getMessage()));
  }

  public Mono<List<GitlabPipelineResponse>> getPipelines(Long gitlabProjectId, String version) {
    return get("/projects/" + gitlabProjectId + "/pipelines?ref=" + version)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<GitlabPipelineResponse>>() {})
            .doOnError(e -> log.error("Failed to fetch pipelines for project {}: {}", gitlabProjectId, e.getMessage()));
  }

  public Mono<List<GitlabPipelineJobResponse>> getPipelineJobs(Long gitlabProjectId, Long pipelineId) {
    return get("/projects/" + gitlabProjectId + "/pipelines/" + pipelineId + "/jobs")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<GitlabPipelineJobResponse>>() {})
            .doOnError(e -> log.error("Failed to fetch pipeline jobs for project {}, pipeline {}: {}", gitlabProjectId, pipelineId, e.getMessage()));
  }
}
