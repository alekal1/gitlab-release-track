package ee.aleksale.releaseapp.model.domain;

import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "releases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String gitlabProjectName;

  @Column(nullable = false)
  private String version;

  private String gitHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PipelineType pipelineType;

  @Enumerated(EnumType.STRING)
  private PipelineStatus pipelineStatus;

  private Long pipelineId;

  @Column(length = 2000)
  private String notes;

  @Column(nullable = false)
  private LocalDate releaseDate;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (releaseDate == null) {
      releaseDate = LocalDate.now();
    }
  }
}
