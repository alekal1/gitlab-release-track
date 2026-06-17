package ee.aleksale.releaseapp.model.dto.response.jira;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssuesResponse {

  private List<JiraIssue> issues;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JiraIssue {
    private String key;
    @JsonProperty(value = "summary")
    private String title;
  }
}
