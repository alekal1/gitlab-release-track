package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.config.GitlabConfig;
import ee.aleksale.releaseapp.event.PipelineUpdateEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.utils.AsyncUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PipelineMonitorService {

  private static final String FAILED_PIPELINE = "failed";
  private static final String SUCCESS_PIPELINE = "success";

  private final ReleaseService releaseService;
  private final GitlabProjectService gitlabProjectService;
  private final GitlabPipelineService gitlabPipelineService;
  private final ApplicationEventPublisher eventPublisher;
  private final GitlabConfig gitlabConfig;

  @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
  public void checkPipelineStatus() {
    var runningOrPendingPipelines = releaseService.findPendingOrRunning();

    for (var release : runningOrPendingPipelines) {
      checkReleasePipeline(release);
    }
  }

  private void checkReleasePipeline(Release release) {
    final var projectId = gitlabProjectService.resolveProjectId(release.getGitlabProjectName());
    AsyncUtils.platformRunLater(
            () -> gitlabPipelineService.getPipelines(projectId, release.getVersion()),
            pipelines -> {
              if (pipelines == null || pipelines.isEmpty()) {
                return;
              }

              var tagPipeline = pipelines.getFirst();
              release.setPipelineId(tagPipeline.getId());

              switch (tagPipeline.getStatus()) {
                case FAILED_PIPELINE -> {
                  release.setPipelineStatus(PipelineStatus.FAILED);
                  releaseService.updateRelease(release);
                  eventPublisher.publishEvent(new PipelineUpdateEvent(this, release));
                }
                case SUCCESS_PIPELINE -> checkGitlabLastStep(projectId, release);
                default -> {
                  release.setPipelineStatus(PipelineStatus.RUNNING);
                  releaseService.updateRelease(release);
                  eventPublisher.publishEvent(new PipelineUpdateEvent(this, release));
                }
              }
            }
    );
  }

  private void checkGitlabLastStep(Long gitlabProjectId, Release release) {
    AsyncUtils.platformRunLater(
            () -> gitlabPipelineService.getPipelineJobs(gitlabProjectId, release.getPipelineId()),
            jobs -> {
              var type = PipelineType.UNSET;

              for (var job : jobs) {
                if (gitlabConfig.getLastStep().equals(job.getName())) {
                  if (SUCCESS_PIPELINE.equals(job.getStatus())) {
                    type = PipelineType.AUTO;
                  } else {
                    type = PipelineType.MANUAL;
                  }
                  break;
                }
              }

              release.setPipelineType(type);
              release.setPipelineStatus(PipelineStatus.SUCCESS);
              releaseService.updateRelease(release);
              eventPublisher.publishEvent(new PipelineUpdateEvent(this, release));
            }
    );
  }
}
