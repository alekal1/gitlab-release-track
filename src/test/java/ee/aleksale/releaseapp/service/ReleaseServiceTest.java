package ee.aleksale.releaseapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ee.aleksale.releaseapp.event.ReleaseDeletedEvent;
import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import ee.aleksale.releaseapp.model.domain.ReleaseEntity;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.repository.GitlabProjectRepository;
import ee.aleksale.releaseapp.repository.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {

  private ReleaseRepository releaseRepository;

  private ReleaseService releaseService;

  @BeforeEach
  void init() {
    releaseRepository = mock(ReleaseRepository.class);
    releaseService = new ReleaseService(releaseRepository, mock(GitlabProjectRepository.class));
  }

  @Test
  void shouldNotSave_whenOnReleaseSaved_withExistingRelease() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .releaseDate(LocalDate.of(2026, 1, 1))
        .pipelineStatus(PipelineStatus.PENDING)
        .build();

    doReturn(true)
        .when(releaseRepository)
        .existsByGitlabProjectNameAndVersionAndReleaseDate("my-project", "1.0.0", LocalDate.of(2026, 1, 1));

    releaseService.onReleaseSaved(new ReleaseSavedEvent(this, release));

    verify(releaseRepository, never()).save(any());
  }

  @Test
  void shouldSave_whenOnReleaseSaved_withNewRelease() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("2.0.0")
        .pipelineType(PipelineType.AUTO)
        .pipelineStatus(PipelineStatus.SUCCESS)
        .build();

    doReturn(false)
        .when(releaseRepository)
        .existsByGitlabProjectNameAndVersionAndReleaseDate("my-project", "2.0.0", null);

    releaseService.onReleaseSaved(new ReleaseSavedEvent(this, release));

    final var captor = ArgumentCaptor.forClass(ReleaseEntity.class);
    verify(releaseRepository).save(captor.capture());
    assertEquals("my-project", captor.getValue().getGitlabProjectName());
    assertEquals("2.0.0", captor.getValue().getVersion());
    assertEquals(PipelineStatus.SUCCESS, captor.getValue().getPipelineStatus());
  }

  @Test
  void shouldSetPendingStatus_whenOnReleaseSaved_withNullPipelineStatus() {
    final var release = Release.builder()
        .gitlabProjectName("my-project")
        .version("3.0.0")
        .pipelineType(PipelineType.AUTO)
        .pipelineStatus(null)
        .build();

    doReturn(false)
        .when(releaseRepository)
        .existsByGitlabProjectNameAndVersionAndReleaseDate("my-project", "3.0.0", null);

    releaseService.onReleaseSaved(new ReleaseSavedEvent(this, release));

    verify(releaseRepository).save(any(ReleaseEntity.class));
    assertEquals(PipelineStatus.PENDING, release.getPipelineStatus());
  }

  @Test
  void shouldDeleteRelease_whenOnReleaseDeleted() {
    final var release = Release.builder()
        .id(5L)
        .build();

    releaseService.onReleaseDeleted(new ReleaseDeletedEvent(this, release));

    verify(releaseRepository).deleteById(5L);
  }

  @Test
  void shouldReturnDistinctDates_whenGetReleaseDates() {
    final var dates = Set.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));
    doReturn(dates).when(releaseRepository).findDistinctReleaseDates();

    final var result = releaseService.getReleaseDates();

    assertEquals(2, result.size());
    assertTrue(result.contains(LocalDate.of(2026, 1, 1)));
  }

  @Test
  void shouldReturnMappedReleases_whenGetReleasesByDateAndService() {
    final var date = LocalDate.of(2026, 4, 17);
    final var entity = ReleaseEntity.builder()
        .id(1L)
        .gitlabProjectName("my-project")
        .version("1.0.0")
        .pipelineType(PipelineType.AUTO)
        .pipelineStatus(PipelineStatus.SUCCESS)
        .releaseDate(date)
        .createdAt(LocalDateTime.now())
        .build();

    doReturn(List.of(entity))
        .when(releaseRepository)
        .findByReleaseDateOrderByCreatedAtDesc(date);

    final var result = releaseService.getReleasesByDateAndService(date);

    assertEquals(1, result.size());
    assertEquals("my-project", result.getFirst().getGitlabProjectName());
    assertEquals("1.0.0", result.getFirst().getVersion());
  }

  @Test
  void shouldReturnEmptyList_whenGetReleasesByDateAndService_withNoReleases() {
    final var date = LocalDate.of(2026, 5, 1);
    doReturn(List.of())
        .when(releaseRepository)
        .findByReleaseDateOrderByCreatedAtDesc(date);

    final var result = releaseService.getReleasesByDateAndService(date);

    assertTrue(result.isEmpty());
  }
}
