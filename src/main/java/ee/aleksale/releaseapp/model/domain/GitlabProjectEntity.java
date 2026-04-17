package ee.aleksale.releaseapp.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "gitlab_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitlabProjectEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private Long gitlabProjectId;

  @Column(nullable = false)
  private String name;

  private String nameWithNamespace;

  private String webUrl;

  @Column(nullable = false)
  private LocalDateTime addedAt;

  @PrePersist
  public void prePersist() {
    if (addedAt == null) {
      addedAt = LocalDateTime.now();
    }
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GitlabProjectEntity that = (GitlabProjectEntity) o;
    return Objects.equals(gitlabProjectId, that.gitlabProjectId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gitlabProjectId);
  }
}
