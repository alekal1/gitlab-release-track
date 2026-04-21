package ee.aleksale.releaseapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineJobResponse;
import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineResponse;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class GitlabPipelineServiceTest {

  private GitlabApiService gitlabApiService;

  private GitlabPipelineService gitlabPipelineService;

  @BeforeEach
  void init() {
    gitlabApiService = mock(GitlabApiService.class);
    gitlabPipelineService = new GitlabPipelineService(gitlabApiService);
  }

  @Test
  void shouldReturnPipelines_whenGetPipelines_withValidProjectIdAndVersion() {
    final var pipelineA = new GitlabPipelineResponse(100L, "success");
    final var pipelineB = new GitlabPipelineResponse(101L, "failed");

    doReturn(Mono.just(List.of(pipelineA, pipelineB)))
        .when(gitlabApiService)
        .getPipelines(10L, "v1.0.0");

    final var result = gitlabPipelineService.getPipelines(10L, "v1.0.0");

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(100L, result.get(0).getId());
    assertEquals("success", result.get(0).getStatus());
    assertEquals(101L, result.get(1).getId());
    assertEquals("failed", result.get(1).getStatus());
  }

  @Test
  void shouldReturnEmptyList_whenGetPipelines_withNoPipelines() {
    doReturn(Mono.just(List.of()))
        .when(gitlabApiService)
        .getPipelines(99L, "v9.0.0");

    final var result = gitlabPipelineService.getPipelines(99L, "v9.0.0");

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  void shouldReturnPipelineJobs_whenGetPipelineJobs_withValidIds() {
    final var jobA = new GitlabPipelineJobResponse("build", "success");
    final var jobB = new GitlabPipelineJobResponse("deploy", "manual");

    doReturn(Mono.just(List.of(jobA, jobB)))
        .when(gitlabApiService)
        .getPipelineJobs(10L, 100L);

    final var result = gitlabPipelineService.getPipelineJobs(10L, 100L);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("build", result.get(0).getName());
    assertEquals("success", result.get(0).getStatus());
    assertEquals("deploy", result.get(1).getName());
  }

  @Test
  void shouldReturnEmptyList_whenGetPipelineJobs_withNoJobs() {
    doReturn(Mono.just(List.of()))
        .when(gitlabApiService)
        .getPipelineJobs(99L, 999L);

    final var result = gitlabPipelineService.getPipelineJobs(99L, 999L);

    assertNotNull(result);
    assertEquals(0, result.size());
  }
}

