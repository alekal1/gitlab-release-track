package ee.aleksale.releaseapp.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitlabSearchResponse {
  private Long id;
  private String name;
  @JsonProperty(value = "name_with_namespace")
  private String nameWithNamespace;
  @JsonProperty(value = "web_url")
  private String webUrl;

}
