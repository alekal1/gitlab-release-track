package ee.aleksale.releaseapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitlabPipelineResponse {
  private Long id;
  private String status;
}
