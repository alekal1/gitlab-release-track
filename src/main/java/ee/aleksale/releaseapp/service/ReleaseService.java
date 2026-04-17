package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.model.mapper.ReleaseMapper;
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

  @Transactional
  @EventListener
  public void onReleaseSaved(ReleaseSavedEvent event) {
    saveRelease(event.getRelease());
  }

  private void saveRelease(Release release) {
    if (releaseRepository.existsByGitlabProjectNameAndVersion(release.getGitlabProjectName(), release.getVersion())) {
      return;
    }

    if (release.getPipelineStatus() == null) {
      release.setPipelineStatus(PipelineStatus.PENDING);
    }
    releaseRepository.save(ReleaseMapper.INSTANCE.toReleaseEntity(release));
//    return ReleaseMapper.INSTANCE.toRelease(savedEntity);
  }

  public Set<LocalDate> getReleaseDates() {
    return releaseRepository.findDistinctReleaseDates();
  }

  public List<Release> getReleasesByDateAndService(LocalDate date) {
    return releaseRepository.findByReleaseDateOrderByCreatedAtDesc(date).stream()
            .map(ReleaseMapper.INSTANCE::toRelease)
            .collect(Collectors.toList());
  }
}
