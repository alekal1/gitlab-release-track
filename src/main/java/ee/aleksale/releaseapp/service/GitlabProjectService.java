package ee.aleksale.releaseapp.service;

import ee.aleksale.releaseapp.repository.GitlabProjectRepository;
import ee.aleksale.releaseapp.service.external.GitlabApiService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitlabProjectService {

  private final GitlabProjectRepository gitlabProjectRepository;
  private final GitlabApiService gitlabApiService;

  @Transactional
  public void saveProject() {
    throw new UnsupportedOperationException();
  }

  public List<?> getSavedProjects() {
    throw new UnsupportedOperationException();
  }

  public List<?> searchProject(String query) {
    throw new UnsupportedOperationException();
  }

}
