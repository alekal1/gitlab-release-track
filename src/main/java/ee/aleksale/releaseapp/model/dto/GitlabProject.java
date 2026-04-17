package ee.aleksale.releaseapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitlabProject {

  private Long gitlabProjectId;
  private String name;
  private String nameWithNamespace;
  private String webUrl;
}
