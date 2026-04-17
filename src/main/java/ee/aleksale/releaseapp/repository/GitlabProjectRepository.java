package ee.aleksale.releaseapp.repository;

import ee.aleksale.releaseapp.model.domain.GitlabProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;


@Repository
public interface GitlabProjectRepository extends JpaRepository<GitlabProjectEntity, Long> {

  Collection<GitlabProjectEntity> findAllByOrderByNameAsc();
  Optional<GitlabProjectEntity> findByGitlabProjectId(Long gitlabProjectId);

}
