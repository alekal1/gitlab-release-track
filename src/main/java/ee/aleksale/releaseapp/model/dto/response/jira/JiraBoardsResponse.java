package ee.aleksale.releaseapp.model.dto.response.jira;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraBoardsResponse {
  @JsonProperty(value = "values")
  private List<JiraBoard> boards;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JiraBoard {
    private Long id;
    private String name;
  }
}
