package ee.aleksale.releaseapp.repository;

import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.domain.ReleaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ReleaseRepository extends JpaRepository<ReleaseEntity, Long> {

  @Query("SELECT DISTINCT r.releaseDate FROM ReleaseEntity r")
  Set<LocalDate> findDistinctReleaseDates();

  List<ReleaseEntity> findByReleaseDateOrderByCreatedAtDesc(LocalDate releaseDate);

  boolean existsByGitlabProjectNameAndVersion(String gitlabProjectName, String version);

  List<ReleaseEntity> findByPipelineStatusIn(Collection<PipelineStatus> pipelineStatuses);
}
