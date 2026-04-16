package ee.aleksale.releaseapp.repository;

import ee.aleksale.releaseapp.domain.GitlabProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GitlabProjectRepository extends JpaRepository<GitlabProject, Long> {
}
