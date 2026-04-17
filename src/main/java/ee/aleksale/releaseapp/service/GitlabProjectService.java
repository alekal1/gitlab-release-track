package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.model.domain.GitlabProjectEntity;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.model.mapper.GitlabProjectMapper;
import ee.aleksale.releaseapp.repository.GitlabProjectRepository;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitlabProjectService {

  private final GitlabProjectRepository gitlabProjectRepository;
  private final GitlabApiService gitlabApiService;

  @Transactional
  public GitlabProject saveProject(GitlabProject gitlabProject) {
    final var existing = gitlabProjectRepository.findByGitlabProjectId(gitlabProject.getGitlabProjectId());
    if (existing.isPresent()) {
      log.info("Project {} already saved", gitlabProject.getName());
      return GitlabProjectMapper.INSTANCE.toGitlabProject(existing.get());
    }

    log.info("Adding project: {} ({})", gitlabProject.getName(), gitlabProject.getGitlabProjectId());
    final var savedEntity = gitlabProjectRepository.saveAndFlush(
            GitlabProjectMapper.INSTANCE.toGitlabProjectEntity(gitlabProject)
    );

    return GitlabProjectMapper.INSTANCE.toGitlabProject(savedEntity);
  }

  public List<GitlabProject> getSavedProjects() {
    return gitlabProjectRepository.findAllByOrderByNameAsc()
            .stream()
            .map(GitlabProjectMapper.INSTANCE::toGitlabProject)
            .collect(Collectors.toList());
  }

  public List<GitlabProject> searchProject(String projectName) {
    final var results = gitlabApiService.searchProjects(projectName).block();
    if (results == null) {
      return List.of();
    }

    return results.stream().map(v -> GitlabProject.builder()
            .gitlabProjectId(v.getId())
            .name(v.getName())
            .nameWithNamespace(v.getNameWithNamespace())
            .webUrl(v.getWebUrl())
            .build())
        .collect(Collectors.toList());
  }

  public Long resolveProjectId(String projectName) {
    return gitlabProjectRepository.findByName(projectName)
            .map(GitlabProjectEntity::getGitlabProjectId)
            .orElse(null);
  }

}
