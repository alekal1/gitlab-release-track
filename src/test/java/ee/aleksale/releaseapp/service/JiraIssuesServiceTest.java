package ee.aleksale.releaseapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ee.aleksale.releaseapp.model.dto.response.jira.JiraIssuesResponse;
import ee.aleksale.releaseapp.service.external.JiraApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class JiraIssuesServiceTest {

private JiraApiService jiraApiService;

  private JiraIssuesService jiraIssuesService;

  @BeforeEach
  void init() {
    jiraApiService = mock(JiraApiService.class);

    jiraIssuesService = new JiraIssuesService(jiraApiService);
  }

  @Test
  void shouldReturnEmptyList_whenNoIssuesFound() {
    final var response = new JiraIssuesResponse(List.of());

    doReturn(Mono.just(response))
            .when(jiraApiService)
            .getBoardsIssuesOnColumn();

    final var result = jiraIssuesService.getBoardIssuesOnColumn();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }


  @Test
  void shouldReturnAllValues_whenIssueExists() {
    final var j1 = new JiraIssuesResponse.JiraIssue();
    final var j2 = new JiraIssuesResponse.JiraIssue();

    final var response = new JiraIssuesResponse(List.of(j1, j2));

    doReturn(Mono.just(response))
            .when(jiraApiService)
            .getBoardsIssuesOnColumn();

    final var result = jiraIssuesService.getBoardIssuesOnColumn();

    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  void shouldReturnEmptyList_whenResponseIsNull() {
    doReturn(Mono.just(new JiraIssuesResponse(null)))
            .when(jiraApiService)
            .getBoardsIssuesOnColumn();

    final var result = jiraIssuesService.getBoardIssuesOnColumn();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
