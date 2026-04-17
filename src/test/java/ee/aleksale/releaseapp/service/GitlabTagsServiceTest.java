package ee.aleksale.releaseapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ee.aleksale.releaseapp.model.dto.response.GitlabFetchTagsResponse;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class GitlabTagsServiceTest {

  private GitlabApiService gitlabApiService;

  private GitlabTagsService gitlabTagsService;

  @BeforeEach
  void init() {
    gitlabApiService = mock(GitlabApiService.class);
    gitlabTagsService = new GitlabTagsService(gitlabApiService);
  }

  @Test
  void shouldReturnTags_whenGetTagsForProject_withValidProjectId() {
    final var tagA = new GitlabFetchTagsResponse();
    tagA.setName("v1.0.0");

    final var tagB = new GitlabFetchTagsResponse();
    tagB.setName("v2.0.0");

    doReturn(Mono.just(List.of(tagA, tagB)))
        .when(gitlabApiService)
        .getTags(10L);

    final var result = gitlabTagsService.getTagsForProject(10L);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("v1.0.0", result.get(0).getName());
    assertEquals("v2.0.0", result.get(1).getName());
  }

  @Test
  void shouldReturnEmptyList_whenGetTagsForProject_withNoTags() {
    doReturn(Mono.just(List.of()))
        .when(gitlabApiService)
        .getTags(99L);

    final var result = gitlabTagsService.getTagsForProject(99L);

    assertNotNull(result);
    assertEquals(0, result.size());
  }
}
