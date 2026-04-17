package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.utils.AppConstants;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

public class ReleaseForm {

  @Getter
  private VBox form;
  private ComboBox<GitlabProject> serviceCombo;

  public ReleaseForm() {
    createForm();
  }

  private void createForm() {
    form = new VBox(8);
    form.setPadding(new Insets(10));
    form.setPrefWidth(AppConstants.FORM_WIDTH);
    form.getStyleClass().add("form-panel");

    form.getChildren().addAll(createFormContent());
  }

  private Collection<Node> createFormContent() {
    Label title = new Label("New Release");
    title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

    Label projectLabel = new Label("Project:");

    serviceCombo = new ComboBox<>();
    serviceCombo.setMaxWidth(Double.MAX_VALUE);
    serviceCombo.setPromptText("Select saved project");
    serviceCombo.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(GitlabProject item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName() + " (" + item.getGitlabProjectId() + ")");
      }
    });
    serviceCombo.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(GitlabProject item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getName());
      }
    });

    return List.of(
            title,
            projectLabel, serviceCombo
    );
  }


  public void refreshProjectCombo(List<GitlabProject> projects) {
    var selected = serviceCombo.getValue();
    serviceCombo.setItems(FXCollections.observableArrayList(projects));
    if (selected != null) {
      projects.stream()
              .filter(p -> p.getGitlabProjectId().equals(selected.getGitlabProjectId()))
              .findFirst()
              .ifPresent(serviceCombo::setValue);
    }
  }
}
