package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.event.ReleaseDeletedEvent;
import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.model.mapper.ReleaseMapper;
import ee.aleksale.releaseapp.repository.GitlabProjectRepository;
import ee.aleksale.releaseapp.repository.ReleaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseService {

  private final ReleaseRepository releaseRepository;
  private final GitlabProjectRepository gitlabProjectRepository;

  @Transactional
  @EventListener
  public void onReleaseSaved(ReleaseSavedEvent event) {
    var release = event.getRelease();
    if (releaseRepository.existsByGitlabProjectNameAndVersionAndReleaseDate(release.getGitlabProjectName(), release.getVersion(), release.getReleaseDate())) {
      return;
    }

    if (release.getPipelineStatus() == null) {
      release.setPipelineStatus(PipelineStatus.PENDING);
    }
    releaseRepository.save(ReleaseMapper.INSTANCE.toReleaseEntity(release));
  }

  @Transactional
  @EventListener
  public void onReleaseDeleted(ReleaseDeletedEvent event) {
    releaseRepository.deleteById(event.getRelease().getId());
  }

  public List<Release> findPendingOrRunning() {
    return releaseRepository.findByPipelineStatusIn(List.of(PipelineStatus.PENDING, PipelineStatus.RUNNING)).stream()
            .map(ReleaseMapper.INSTANCE::toRelease)
            .collect(Collectors.toList());
  }


  public Set<LocalDate> getReleaseDates() {
    return releaseRepository.findDistinctReleaseDates();
  }

  public List<Release> getReleasesByDate(LocalDate date) {
    return releaseRepository.findByReleaseDateOrderByCreatedAtDesc(date).stream()
            .map(ReleaseMapper.INSTANCE::toRelease)
            .peek(release -> gitlabProjectRepository.findByName(release.getGitlabProjectName())
                    .ifPresent(project -> release.setGitlabProjectWebUrl(project.getWebUrl())))
            .collect(Collectors.toList());
  }

  @Transactional
  public void updateRelease(Release release) {
    final var entity = ReleaseMapper.INSTANCE.toReleaseEntity(release);
    releaseRepository.saveAndFlush(entity);
  }
}
