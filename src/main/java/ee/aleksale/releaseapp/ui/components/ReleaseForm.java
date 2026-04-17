package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.service.GitlabProjectService;
import ee.aleksale.releaseapp.service.GitlabTagsService;
import ee.aleksale.releaseapp.utils.AppConstants;
import ee.aleksale.releaseapp.utils.AsyncUtils;
import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
    form = new VBox(8);
    form.setPadding(new Insets(10));
    form.setPrefWidth(AppConstants.FORM_WIDTH);
    form.getStyleClass().add("form-panel");

    initComponents();
    form.getChildren().addAll(
            createTitle(),
            new Label("Project:"), serviceCombo, createSearchBox(), new Separator(),
            new Label("Version / Tag :"), createVersionBox(),
            new Label("Git Hash:"), hashField,
            new Label("Notes:"), notesField,
            new Separator(),
            createSaveButton()
    );
  }

  private void initComponents() {
    serviceCombo = createProjectCombo();
    versionCombo = createVersionCombo();
    hashField = createHashField();
    notesField = new TextField();
    notesField.setPromptText("Notes");
  }

  private Label createTitle() {
    var title = new Label("New Release");
    title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
    return title;
  }

  private ComboBox<GitlabProject> createProjectCombo() {
    var combo = new ComboBox<GitlabProject>();
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setPromptText("Select saved project");
    combo.setCellFactory(lv -> projectCell(p -> p.getName() + " (" + p.getGitlabProjectId() + ")"));
    combo.setButtonCell(projectCell(GitlabProject::getName));
    refreshProjectCombo(combo);

    combo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && !newVal.equals(oldVal)) {
        loadTags(newVal);
      } else if (newVal == null && versionCombo != null) {
        versionCombo.getItems().clear();
        tagHashMap.clear();
      }
    });
    return combo;
  }

  private ListCell<GitlabProject> projectCell(java.util.function.Function<GitlabProject, String> formatter) {
    return new ListCell<>() {
      @Override
      protected void updateItem(GitlabProject item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : formatter.apply(item));
      }
    };
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

  private ComboBox<String> createVersionCombo() {
    var combo = new ComboBox<String>();
    combo.setEditable(true);
    combo.setPromptText("Select tag or type version");
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && tagHashMap.containsKey(newVal)) {
        hashField.setText(tagHashMap.get(newVal));
      }
    });
    return combo;
  }

  private HBox createVersionBox() {
    var refreshTagsBtn = new Button(AppConstants.REFRESH_ICON);
    refreshTagsBtn.setTooltip(new Tooltip(AppConstants.REFRESH_TOOLTIP));
    refreshTagsBtn.setOnAction(e -> loadTags(serviceCombo.getValue()));

    var box = new HBox(5, versionCombo, refreshTagsBtn);
    box.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(versionCombo, Priority.ALWAYS);
    return box;
  }

  private TextField createHashField() {
    var field = new TextField();
    field.setPromptText("Git hash (auto-filled from tag)");
    field.setEditable(false);
    return field;
  }

  private Button createSaveButton() {
    var btn = new Button("Save Release");
    btn.setMaxWidth(Double.MAX_VALUE);
    btn.getStyleClass().add("primary-button");
    btn.setOnAction(e -> createAndSaveRelease());
    return btn;
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

    AsyncUtils.platformRunLater(
            () -> gitlabTagsService.getTagsForProject(selectedProject.getGitlabProjectId()),
            tags -> {
              if (tags == null || tags.isEmpty()) {
                return;
              }
              for (var t : tags) {
                tagHashMap.put(t.getName(), t.getCommit().getId());
                versionCombo.getItems().add(t.getName());
              }
            }
    );
  }

  private void searchProjects(String query) {
    if (StringUtils.isBlank(query)) {
      return;
    }
    AsyncUtils.platformRunLater(
            () -> gitlabProjectService.searchProject(query),
            this::showSearchResults
    );
  }

  private void createAndSaveRelease() {
    var project = serviceCombo.getValue();
    if (project == null) {
      return;
    }

    var version = getVersionText();
    if (StringUtils.isBlank(version)) {
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
    eventPublisher.publishEvent(new ReleaseSavedEvent(this, release));
  }

  private void showSearchResults(List<GitlabProject> results) {
    if (results.isEmpty()) {
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
    listView.setCellFactory(lv -> projectCell(p -> p.getName() + "  [ID: " + p.getGitlabProjectId() + "]"));
    listView.setPrefHeight(AppConstants.PROJECT_CHOOSE_DIALOG_HEIGHT);
    listView.setPrefWidth(AppConstants.PROJECT_CHOOSE_DIALOG_WIDTH);

    dialog.getDialogPane().setContent(listView);
    dialog.setResultConverter(btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedItem() : null);

    dialog.showAndWait().ifPresent(this::addAndSelectProject);
  }

  private void addAndSelectProject(GitlabProject project) {
    var saved = gitlabProjectService.saveProject(project);
    refreshProjectCombo(serviceCombo);
    serviceCombo.getItems().stream()
            .filter(p -> p.getGitlabProjectId().equals(saved.getGitlabProjectId()))
            .findFirst()
            .ifPresent(serviceCombo::setValue);
    loadTags(serviceCombo.getValue());
  }

  private void refreshProjectCombo(ComboBox<GitlabProject> combo) {
    var selected = combo.getValue();
    var projects = gitlabProjectService.getSavedProjects();
    combo.setItems(FXCollections.observableArrayList(projects));

    if (selected != null) {
      projects.stream()
              .filter(p -> p.getGitlabProjectId().equals(selected.getGitlabProjectId()))
              .findFirst()
              .ifPresent(combo::setValue);
    }
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
