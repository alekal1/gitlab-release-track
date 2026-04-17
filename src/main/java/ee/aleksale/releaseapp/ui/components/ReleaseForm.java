package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.service.GitlabProjectService;
import ee.aleksale.releaseapp.service.GitlabTagsService;
import ee.aleksale.releaseapp.utils.AppConstants;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReleaseForm {

  @Getter
  private VBox form;
  private ComboBox<GitlabProject> serviceCombo;
  private ComboBox<String> versionCombo;
  private final Map<String, String> tagHashMap = new HashMap<>();
  private TextField hashField;
  private TextField notesField;

  private final ReleaseDatePicker releaseDatePicker;
  private final GitlabTagsService gitlabTagsService;
  private final GitlabProjectService gitlabProjectService;
  private final ApplicationEventPublisher eventPublisher;

  @PostConstruct
  void initForm() {
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
    refreshProjectCombo();

    serviceCombo.setOnAction(e -> {
      var selectedProject = getSelectedProject();

      if (selectedProject != null) {
        loadTags(selectedProject);
      } else if (versionCombo != null) {
        versionCombo.getItems().clear();
        tagHashMap.clear();
      }
    });

    var searchField = new TextField();
    searchField.setPromptText("Search GitLab projects...");

    var searchBtn = new Button("Search API");
    searchBtn.setMaxWidth(Double.MAX_VALUE);
    searchBtn.setOnAction(e -> searchProjects(searchField.getText().trim()));

    var searchBox = new HBox(5, searchField, searchBtn);
    searchBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(searchField, Priority.ALWAYS);

    versionCombo = new ComboBox<>();
    versionCombo.setEditable(true);
    versionCombo.setPromptText("Select tag or type version");
    versionCombo.setMaxWidth(Double.MAX_VALUE);
    versionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && tagHashMap.containsKey(newVal)) {
        hashField.setText(tagHashMap.get(newVal));
      }
    });

    var refreshTagsBtn = new Button(AppConstants.REFRESH_ICON);
    refreshTagsBtn.setTooltip(new Tooltip(AppConstants.REFRESH_TOOLTIP));
    refreshTagsBtn.setOnAction(e -> {
      var project = getSelectedProject();
      loadTags(project);
    });

    HBox versionBox = new HBox(5, versionCombo, refreshTagsBtn);
    versionBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(versionCombo, Priority.ALWAYS);

    hashField = new TextField();
    hashField.setPromptText("Git hash (auto-filled from tag)");
    hashField.setEditable(false);

    notesField = new TextField();
    notesField.setPromptText("Notes");

    var saveBtn = new Button("Save Release");
    saveBtn.setMaxWidth(Double.MAX_VALUE);
    saveBtn.getStyleClass().add("primary-button");
    saveBtn.setOnAction(e -> createAndSaveRelease());

    return List.of(
            title,
            projectLabel, serviceCombo, searchBox, new Separator(),
            new Label("Version / Tag :"), versionBox,
            new Label("Git Hash:"), hashField,
            new Label("Notes:"), notesField,
            new Separator(),
            saveBtn
    );
  }


  private void loadTags(GitlabProject selectedProject) {
    if (selectedProject == null) {
      return;
    }

    tagHashMap.clear();
    versionCombo.getItems().clear();
    versionCombo.setValue(null);
    versionCombo.getEditor().clear();
    hashField.clear();

    Thread.ofVirtual().start(() -> {
      var tags = gitlabTagsService.loadTagsForProject(selectedProject.getGitlabProjectId());

      Platform.runLater(() -> {

        if (tags == null || tags.isEmpty()) {
//            setStatus("No tags found for " + selectedProject.getName());
          return;
        }
        for (var t : tags) {
          tagHashMap.put(t.getName(), t.getCommit().getId());
          versionCombo.getItems().add(t.getName());
        }
//          setStatus("Loaded " + tags.size() + " tags for " + selectedProject.getName());
      });
    });
  }

  private void searchProjects(String query) {
    if (StringUtils.isBlank(query)) {
//      setStatus("Enter a search term");
      return;
    }
//    setStatus("Searching GitLab for '" + query + "'...");
    Thread.ofVirtual().start(() -> {
      try {
        var results = gitlabProjectService.searchProject(query);
        Platform.runLater(() -> showSearchResults(results));
      } catch (Exception e) {
//        Platform.runLater(() -> setStatus("Search failed: " + e.getMessage()));
      }
    });
  }

  private void createAndSaveRelease() {
    var project = getSelectedProject();
    if (project == null) {
      return;
    }

    var version = getVersionText();
    if (StringUtils.isBlank(version)) {
//      setStatus("Version is required");
      return;
    }

    var release = Release.builder()
            .gitlabProjectName(project.getName())
            .version(version)
            .gitHash(hashField.getText().trim())
            .pipelineType(PipelineType.UNSET)
            .pipelineStatus(PipelineStatus.PENDING)
            .notes(notesField.getText().trim())
            .releaseDate(getSelectedDate())
            .build();
    clearForm();
    // setStatus("Release saved: " + project.getName() + " " + version + " for " + getSelectedDate());
    eventPublisher.publishEvent(new ReleaseSavedEvent(this, release));
  }

  private void showSearchResults(List<GitlabProject> results) {
    if (results.isEmpty()) {
//      setStatus("No projects found");
      return;
    }

    if (results.size() == 1) {
      addAndSelectProject(results.getFirst());
      return;
    }

    var dialog = new Dialog<GitlabProject>();
    dialog.setTitle(AppConstants.PROJECT_CHOOSE_DIALOG_TITLE);
    dialog.setHeaderText(AppConstants.PROJECT_CHOOSE_HEADER_TEXT);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    ListView<GitlabProject> listView = new ListView<>(FXCollections.observableArrayList(results));
    listView.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(GitlabProject item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item.getName() + "  [ID: " + item.getGitlabProjectId() + "]");
        }
      }
    });
    listView.setPrefHeight(AppConstants.PROJECT_CHOOSE_DIALOG_HEIGHT);
    listView.setPrefWidth(AppConstants.PROJECT_CHOOSE_DIALOG_WIDTH);

    dialog.getDialogPane().setContent(listView);
    dialog.setResultConverter(btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedItem() : null);

    dialog.showAndWait().ifPresent(this::addAndSelectProject);
  }

  private void addAndSelectProject(GitlabProject project) {
    var saved = gitlabProjectService.saveProject(project);
    refreshProjectCombo();
    serviceCombo.getItems().stream()
            .filter(p -> p.getGitlabProjectId().equals(saved.getGitlabProjectId()))
            .findFirst()
            .ifPresent(serviceCombo::setValue);
    loadTags(getSelectedProject());
//    setStatus("Added project: " + saved.getName());
  }

  private void refreshProjectCombo() {
    var selected = serviceCombo.getValue();

    var projects = gitlabProjectService.getSavedProjects();
    serviceCombo.setItems(FXCollections.observableArrayList(projects));

    if (selected != null) {
      projects.stream()
              .filter(p -> p.getGitlabProjectId().equals(selected.getGitlabProjectId()))
              .findFirst()
              .ifPresent(serviceCombo::setValue);
    }
  }

  private GitlabProject getSelectedProject() {
    var project = serviceCombo.getValue();
    if (project == null) {
//      setStatus("Select a project first");
      return null;
    }
    return project;
  }

  private String getVersionText() {
    String text = versionCombo.getEditor().getText();
    if (text != null && !text.isBlank()) {
      return text.trim();
    }
    return versionCombo.getValue() != null ? versionCombo.getValue().trim() : "";
  }

  private void clearForm() {
    versionCombo.setValue(null);
    versionCombo.getEditor().clear();
    hashField.clear();
    notesField.clear();
  }

  private LocalDate getSelectedDate() {
    var datePicker = releaseDatePicker.getDatePicker();
    return datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
  }
}
