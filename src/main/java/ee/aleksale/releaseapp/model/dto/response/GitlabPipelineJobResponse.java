package ee.aleksale.releaseapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitlabPipelineJobResponse {
  private String name;
  private String status;
}
