package ee.aleksale.releaseapp.model.dto;

import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Release {

  private String gitlabProjectName;
  private String version;
  private String gitHash;
  private PipelineType pipelineType;
  private PipelineStatus pipelineStatus;
  private Long pipelineId;
  private String notes;
  private LocalDateTime createdAt;
}
