package ee.aleksale.releaseapp.ui.components.form;

import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.service.GitlabProjectService;
import ee.aleksale.releaseapp.ui.components.dialog.ProjectSearchResultDialog;
import ee.aleksale.releaseapp.utils.AsyncUtils;
import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSelectionComponent {

  @Getter
  private VBox content;

  private ComboBox<GitlabProject> projectCombo;
  private Consumer<GitlabProject> onProjectSelected = project -> {};

  private final ProjectSearchResultDialog projectSearchResultDialog;

  private final GitlabProjectService gitlabProjectService;
  private final ApplicationEventPublisher eventPublisher;

  @PostConstruct
  void init() {
    projectCombo = createProjectCombo();

    content = new VBox(5,
            new Label("Project:"),
            projectCombo,
            createSearchBox()
    );
  }

  public GitlabProject getSelectedProject() {
    return projectCombo.getValue();
  }

  public void setOnProjectSelected(Consumer<GitlabProject> onProjectSelected) {
    this.onProjectSelected = onProjectSelected != null ? onProjectSelected : project -> {};
  }

  private ComboBox<GitlabProject> createProjectCombo() {
    var combo = new ComboBox<GitlabProject>();
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setPromptText("Select saved project");
    combo.setCellFactory(lv -> GitlabProject.projectCell(project -> project.getName() + " (" + project.getGitlabProjectId() + ")"));
    combo.setButtonCell(GitlabProject.projectCell(GitlabProject::getName));
    refreshProjectCombo(combo);

    combo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && !newVal.equals(oldVal)) {
        onProjectSelected.accept(newVal);
      } else if (newVal == null) {
        onProjectSelected.accept(null);
      }
    });
    return combo;
  }

  private HBox createSearchBox() {
    var searchField = new TextField();
    searchField.setPromptText("Search GitLab projects...");

    var searchBtn = new Button("Search API");
    searchBtn.setMaxWidth(Double.MAX_VALUE);
    searchBtn.setOnAction(e -> searchProjects(searchField.getText().trim()));

    var box = new HBox(5, searchField, searchBtn);
    box.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    return box;
  }

  private void searchProjects(String query) {
    if (StringUtils.isBlank(query)) {
      return;
    }

    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Searching GitLab for '" + query + "'..."));

    AsyncUtils.platformRunLater(
            () -> gitlabProjectService.searchProject(query),
            this::showSearchResults
    );
  }

  private void showSearchResults(List<GitlabProject> results) {
    if (results == null || results.isEmpty()) {
      eventPublisher.publishEvent(new StatusUpdateEvent(this, "No projects found"));
      return;
    }

    if (results.size() == 1) {
      addAndSelectProject(results.getFirst());
      return;
    }

    projectSearchResultDialog.showSearchResultDialog(results)
            .ifPresent(this::addAndSelectProject);
  }

  private void addAndSelectProject(GitlabProject project) {
    var saved = gitlabProjectService.saveProject(project);
    projectCombo.setValue(null);
    refreshProjectCombo(projectCombo);
    projectCombo.getItems().stream()
            .filter(savedProject -> savedProject.getGitlabProjectId().equals(saved.getGitlabProjectId()))
            .findFirst()
            .ifPresent(projectCombo::setValue);
    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Added project: " + saved.getName()));
  }

  private void refreshProjectCombo(ComboBox<GitlabProject> combo) {
    var selected = combo.getValue();
    var projects = gitlabProjectService.getSavedProjects();
    combo.setItems(FXCollections.observableArrayList(projects));

    if (selected != null) {
      projects.stream()
              .filter(project -> project.getGitlabProjectId().equals(selected.getGitlabProjectId()))
              .findFirst()
              .ifPresent(combo::setValue);
    }
  }
}

