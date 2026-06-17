package ee.aleksale.releaseapp.service;


import ee.aleksale.releaseapp.model.dto.response.jira.JiraIssuesResponse;
import ee.aleksale.releaseapp.service.external.JiraApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraIssuesService {

  private final JiraApiService jiraApiService;

  public List<JiraIssuesResponse.JiraIssue> getBoardIssuesOnColumn() {
    final var issuesResponse = jiraApiService.getBoardsIssuesOnColumn().block();
    if (issuesResponse == null || issuesResponse.getIssues() == null) {
      log.error("No issues found");
      return List.of();
    }

    return issuesResponse.getIssues();
  }
}
