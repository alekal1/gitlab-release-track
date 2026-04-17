package ee.aleksale.releaseapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitlabFetchTagsResponse {
  private String name;
  private Commit commit;

  @Data
  public static class Commit {
    private String id;
  }
}
