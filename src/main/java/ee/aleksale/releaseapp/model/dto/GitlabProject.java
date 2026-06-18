package ee.aleksale.releaseapp.model.dto;

import javafx.scene.control.ListCell;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitlabProject {

  private Long gitlabProjectId;
  private String name;
  private String nameWithNamespace;
  private String webUrl;

  public static ListCell<GitlabProject> projectCell(Function<GitlabProject, String> formatter) {
    return new ListCell<>() {
      @Override
      protected void updateItem(GitlabProject item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : formatter.apply(item));
      }
    };
  }
}
