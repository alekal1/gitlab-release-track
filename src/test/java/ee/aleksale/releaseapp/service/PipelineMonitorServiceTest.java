package ee.aleksale.releaseapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ee.aleksale.releaseapp.config.GitlabConfig;
import ee.aleksale.releaseapp.event.PipelineUpdateEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineJobResponse;
import ee.aleksale.releaseapp.model.dto.response.GitlabPipelineResponse;
import ee.aleksale.releaseapp.utils.AsyncUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class PipelineMonitorServiceTest {

  private ReleaseService releaseService;
  private GitlabProjectService gitlabProjectService;
  private GitlabPipelineService gitlabPipelineService;
  private ApplicationEventPublisher eventPublisher;
  private GitlabConfig gitlabConfig;

  private PipelineMonitorService pipelineMonitorService;
  private MockedStatic<AsyncUtils> asyncUtilsMockedStatic;

  @BeforeEach
  void init() {
    releaseService = mock(ReleaseService.class);
    gitlabProjectService = mock(GitlabProjectService.class);
    gitlabPipelineService = mock(GitlabPipelineService.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    gitlabConfig = new GitlabConfig();
    gitlabConfig.setLastStep("deploy");

    pipelineMonitorService = new PipelineMonitorService(
        releaseService, gitlabProjectService, gitlabPipelineService, eventPublisher, gitlabConfig
    );

    asyncUtilsMockedStatic = mockStatic(AsyncUtils.class);
    asyncUtilsMockedStatic.when(() -> AsyncUtils.platformRunLater(any(Supplier.class), any(Consumer.class)))
        .thenAnswer(invocation -> {
          Supplier<?> supplier = invocation.getArgument(0);
          Consumer consumer = invocation.getArgument(1);
          consumer.accept(supplier.get());
          return null;
        });
  }

  @AfterEach
  void tearDown() {
    asyncUtilsMockedStatic.close();
  }

  @Test
  void shouldDoNothing_whenCheckPipelineStatus_withNoPendingReleases() {
    doReturn(List.of()).when(releaseService).findPendingOrRunning();

    pipelineMonitorService.checkPipelineStatus();

    verify(gitlabProjectService, never()).resolveProjectId(any());
  }

  @Test
  void shouldSetFailed_whenPipelineStatusIsFailed() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(List.of(release)).when(releaseService).findPendingOrRunning();
    doReturn(10L).when(gitlabProjectService).resolveProjectId("my-project");
    doReturn(List.of(new GitlabPipelineResponse(100L, "failed")))
        .when(gitlabPipelineService).getPipelines(10L, "1.0.0");

    pipelineMonitorService.checkPipelineStatus();

    assertEquals(PipelineStatus.FAILED, release.getPipelineStatus());
    assertEquals(100L, release.getPipelineId());
    verify(releaseService).updateRelease(release);
    verify(eventPublisher).publishEvent(any(PipelineUpdateEvent.class));
  }

  @Test
  void shouldSetRunning_whenPipelineStatusIsRunning() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(List.of(release)).when(releaseService).findPendingOrRunning();
    doReturn(10L).when(gitlabProjectService).resolveProjectId("my-project");
    doReturn(List.of(new GitlabPipelineResponse(100L, "running")))
        .when(gitlabPipelineService).getPipelines(10L, "1.0.0");

    pipelineMonitorService.checkPipelineStatus();

    assertEquals(PipelineStatus.RUNNING, release.getPipelineStatus());
    verify(releaseService).updateRelease(release);
  }

  @Test
  void shouldDoNothing_whenPipelinesAreEmpty() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(List.of(release)).when(releaseService).findPendingOrRunning();
    doReturn(10L).when(gitlabProjectService).resolveProjectId("my-project");
    doReturn(List.of()).when(gitlabPipelineService).getPipelines(10L, "1.0.0");

    pipelineMonitorService.checkPipelineStatus();

    verify(releaseService, never()).updateRelease(any());
  }

  @Test
  void shouldSetAutoType_whenLastStepIsSuccess() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(List.of(release)).when(releaseService).findPendingOrRunning();
    doReturn(10L).when(gitlabProjectService).resolveProjectId("my-project");
    doReturn(List.of(new GitlabPipelineResponse(100L, "success")))
        .when(gitlabPipelineService).getPipelines(10L, "1.0.0");
    doReturn(List.of(new GitlabPipelineJobResponse("deploy", "success")))
        .when(gitlabPipelineService).getPipelineJobs(10L, 100L);

    pipelineMonitorService.checkPipelineStatus();

    assertEquals(PipelineStatus.SUCCESS, release.getPipelineStatus());
    assertEquals(PipelineType.AUTO, release.getPipelineType());
    verify(releaseService).updateRelease(release);
  }

  @Test
  void shouldSetManualType_whenLastStepIsNotSuccess() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(List.of(release)).when(releaseService).findPendingOrRunning();
    doReturn(10L).when(gitlabProjectService).resolveProjectId("my-project");
    doReturn(List.of(new GitlabPipelineResponse(100L, "success")))
        .when(gitlabPipelineService).getPipelines(10L, "1.0.0");
    doReturn(List.of(new GitlabPipelineJobResponse("deploy", "manual")))
        .when(gitlabPipelineService).getPipelineJobs(10L, 100L);

    pipelineMonitorService.checkPipelineStatus();

    assertEquals(PipelineStatus.SUCCESS, release.getPipelineStatus());
    assertEquals(PipelineType.MANUAL, release.getPipelineType());
  }

  @Test
  void shouldSetUnknownType_whenLastStepNotFound() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(List.of(release)).when(releaseService).findPendingOrRunning();
    doReturn(10L).when(gitlabProjectService).resolveProjectId("my-project");
    doReturn(List.of(new GitlabPipelineResponse(100L, "success")))
        .when(gitlabPipelineService).getPipelines(10L, "1.0.0");
    doReturn(List.of(new GitlabPipelineJobResponse("build", "success")))
        .when(gitlabPipelineService).getPipelineJobs(10L, 100L);

    pipelineMonitorService.checkPipelineStatus();

    assertEquals(PipelineStatus.SUCCESS, release.getPipelineStatus());
    assertEquals(PipelineType.UNKNOWN, release.getPipelineType());
  }
}

