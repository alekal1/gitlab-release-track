package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.model.dto.response.GitlabFetchTagsResponse;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitlabTagsService {

  private final GitlabApiService gitlabApiService;

  public List<GitlabFetchTagsResponse> getTagsForProject(Long gitlabProjectId) {
     return gitlabApiService.getTags(gitlabProjectId).block();
  }
}
