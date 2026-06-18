package ee.aleksale.releaseapp.model.dto.response.jira;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.control.ListCell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
    private JiraIssueFields fields;

    public static ListCell<JiraIssue> issueCell() {
      return new ListCell<>() {
        @Override
        protected void updateItem(JiraIssuesResponse.JiraIssue item, boolean empty) {
          super.updateItem(item, empty);
          setText(empty || item == null ? null : formatIssue(item));
        }
      };
    }

    public static String formatIssue(JiraIssuesResponse.JiraIssue issue) {
      if (issue == null || StringUtils.isBlank(issue.getKey())) {
        return "";
      }
      var title = issue.getFields() != null ? StringUtils.defaultString(issue.getFields().getTitle()).trim() : "";
      return issue.getKey() + ": " + title;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JiraIssueFields {
    @JsonProperty(value = "summary")
    private String title;
  }
}
