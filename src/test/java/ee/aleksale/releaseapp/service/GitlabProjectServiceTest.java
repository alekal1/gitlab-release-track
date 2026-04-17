package ee.aleksale.releaseapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ee.aleksale.releaseapp.model.domain.GitlabProjectEntity;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.model.dto.GitlabSearchResponse;
import ee.aleksale.releaseapp.repository.GitlabProjectRepository;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GitlabProjectServiceTest {


  private GitlabProjectRepository gitlabProjectRepository;
  private GitlabApiService gitlabApiService;


  private GitlabProjectService gitlabProjectService;

  @BeforeEach
  void init() {
    gitlabProjectRepository = mock(GitlabProjectRepository.class);
    gitlabApiService = mock(GitlabApiService.class);

    gitlabProjectService = new GitlabProjectService(gitlabProjectRepository, gitlabApiService);
  }

  @Test
  void shouldReturnExistingProject_whenSaveProject_withAlreadySavedProject() {
    final var input = GitlabProject.builder()
        .gitlabProjectId(10L)
        .name("release-track")
        .nameWithNamespace("team/release-track")
        .webUrl("https://gitlab.example/team/release-track")
        .build();

    final var existingEntity = GitlabProjectEntity.builder()
        .id(1L)
        .gitlabProjectId(10L)
        .name("release-track")
        .nameWithNamespace("team/release-track")
        .webUrl("https://gitlab.example/team/release-track")
        .addedAt(LocalDateTime.now())
        .build();

    doReturn(Optional.of(existingEntity))
            .when(gitlabProjectRepository)
            .findByGitlabProjectId(10L);

    final var result = gitlabProjectService.saveProject(input);

    assertEquals(existingEntity.getGitlabProjectId(), result.getGitlabProjectId());
    assertEquals(existingEntity.getName(), result.getName());
    assertEquals(existingEntity.getNameWithNamespace(), result.getNameWithNamespace());
    assertEquals(existingEntity.getWebUrl(), result.getWebUrl());
    verify(gitlabProjectRepository, never()).saveAndFlush(any());
  }

  @Test
  void shouldSaveAndReturnProject_whenSaveProject_withNewProject() {
    final var input = GitlabProject.builder()
        .gitlabProjectId(11L)
        .name("new-project")
        .nameWithNamespace("team/new-project")
        .webUrl("https://gitlab.example/team/new-project")
        .build();

    final var entityToSave = GitlabProjectEntity.builder()
        .id(2L)
        .gitlabProjectId(11L)
        .name("new-project")
        .nameWithNamespace("team/new-project")
        .webUrl("https://gitlab.example/team/new-project")
        .addedAt(LocalDateTime.now())
        .build();

    doReturn(Optional.empty())
            .when(gitlabProjectRepository)
            .findByGitlabProjectId(11L);
    doReturn(entityToSave)
            .when(gitlabProjectRepository)
            .saveAndFlush(any(GitlabProjectEntity.class));

    final var result = gitlabProjectService.saveProject(input);

    final var entityCaptor = ArgumentCaptor.forClass(GitlabProjectEntity.class);

    verify(gitlabProjectRepository).saveAndFlush(entityCaptor.capture());
    assertEquals(input.getGitlabProjectId(), entityCaptor.getValue().getGitlabProjectId());
    assertEquals(input.getName(), entityCaptor.getValue().getName());

    assertEquals(entityToSave.getGitlabProjectId(), result.getGitlabProjectId());
    assertEquals(entityToSave.getName(), result.getName());
    assertEquals(entityToSave.getNameWithNamespace(), result.getNameWithNamespace());
    assertEquals(entityToSave.getWebUrl(), result.getWebUrl());
  }

  @Test
  void shouldReturnMappedProjects_whenGetSavedProjects_withRepositoryEntities() {
    final var entityA = GitlabProjectEntity.builder()
        .id(3L)
        .gitlabProjectId(100L)
        .name("Alpha")
        .nameWithNamespace("team/Alpha")
        .webUrl("https://gitlab.example/team/alpha")
        .addedAt(LocalDateTime.now())
        .build();

    final var entityB = GitlabProjectEntity.builder()
        .id(4L)
        .gitlabProjectId(200L)
        .name("Beta")
        .nameWithNamespace("team/Beta")
        .webUrl("https://gitlab.example/team/beta")
        .addedAt(LocalDateTime.now())
        .build();

    doReturn(List.of(entityA, entityB)).when(gitlabProjectRepository).findAllByOrderByNameAsc();

    final var result = gitlabProjectService.getSavedProjects();

    assertEquals(2, result.size());
    assertEquals(100L, result.get(0).getGitlabProjectId());
    assertEquals("Alpha", result.get(0).getName());
    assertEquals(200L, result.get(1).getGitlabProjectId());
    assertEquals("Beta", result.get(1).getName());
  }

  @Test
  void shouldReturnEmptyList_whenSearchProject_withNullApiResponse() {
    doReturn(Mono.empty())
            .when(gitlabApiService)
            .searchProjects("release");

    final var result = gitlabProjectService.searchProject("release");

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnMappedProjects_whenSearchProject_withApiResults() {
    final var apiResultA = new GitlabSearchResponse(
        500L,
        "release-api",
        "team/release-api",
        "https://gitlab.example/team/release-api"
    );
    final var apiResultB = new GitlabSearchResponse(
        600L,
        "release-ui",
        "team/release-ui",
        "https://gitlab.example/team/release-ui"
    );

    doReturn(Mono.just(List.of(apiResultA, apiResultB)))
            .when(gitlabApiService).searchProjects("release");

    final var result = gitlabProjectService.searchProject("release");

    assertEquals(2, result.size());
  }
}
