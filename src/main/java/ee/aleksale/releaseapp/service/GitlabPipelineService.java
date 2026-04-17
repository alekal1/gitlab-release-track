package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineJobResponse;
import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineResponse;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitlabPipelineService {

  private final GitlabApiService gitlabApiService;

  public List<GitlabPipelineResponse> getPipelines(Long gitlabProjectId, String version) {
    return gitlabApiService.getPipelines(gitlabProjectId, version).block();
  }

  public List<GitlabPipelineJobResponse> getPipelineJobs(Long gitlabProjectId, Long pipelineId) {
    return gitlabApiService.getPipelineJobs(gitlabProjectId, pipelineId).block();
  }

}
