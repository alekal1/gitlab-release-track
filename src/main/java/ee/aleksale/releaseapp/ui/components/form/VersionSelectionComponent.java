package ee.aleksale.releaseapp.ui.components.form;

import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.service.GitlabTagsService;
import ee.aleksale.releaseapp.utils.AppConstants;
import ee.aleksale.releaseapp.utils.AsyncUtils;
import jakarta.annotation.PostConstruct;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class VersionSelectionComponent {

  @Getter
  private VBox content;

  private ComboBox<String> versionCombo;
  private final Map<String, String> tagHashMap = new HashMap<>();
  private TextField hashField;
  private Supplier<GitlabProject> selectedProjectSupplier = () -> null;

  private final GitlabTagsService gitlabTagsService;
  private final ApplicationEventPublisher eventPublisher;

  @PostConstruct
  void init() {
    versionCombo = createVersionCombo();
    hashField = createHashField();

    content = new VBox(5,
            new Label("Version / Tag :"),
            createVersionBox(),
            new Label("Git Hash:"),
            hashField
    );
  }

  public void setSelectedProject(Supplier<GitlabProject> projectSupplier) {
    this.selectedProjectSupplier = projectSupplier != null ? projectSupplier : () -> null;
  }

  public void loadTags(GitlabProject selectedProject) {
    if (selectedProject == null) {
      eventPublisher.publishEvent(new StatusUpdateEvent(this, "Select a project first."));
      return;
    }

    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Loading tags for " + selectedProject.getName() + "..."));

    clearAvailableTags();

    AsyncUtils.platformRunLater(
            () -> gitlabTagsService.getTagsForProject(selectedProject.getGitlabProjectId()),
            tags -> {
              if (tags == null || tags.isEmpty()) {
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "No tags found for " + selectedProject.getName()));
                return;
              }
              var tagNames = new ArrayList<String>();
              for (var tag : tags) {
                tagHashMap.put(tag.getName(), tag.getCommit().getId());
                tagNames.add(tag.getName());
              }

              eventPublisher.publishEvent(new StatusUpdateEvent(this,
                      "Loaded " + tags.size() + " tags for " + selectedProject.getName()));
              versionCombo.getItems().setAll(tagNames);
            }
    );
  }

  public void clearSelection() {
    versionCombo.setValue(null);
    versionCombo.getEditor().clear();
    hashField.clear();
  }

  public void clearAvailableTags() {
    tagHashMap.clear();
    versionCombo.getItems().clear();
    clearSelection();
  }

  public String getVersionText() {
    String text = versionCombo.getEditor().getText();
    if (text != null && !text.isBlank()) {
      return text.trim();
    }
    return versionCombo.getValue() != null ? versionCombo.getValue().trim() : "";
  }

  public String getGitHash() {
    return StringUtils.defaultString(hashField.getText()).trim();
  }

  private ComboBox<String> createVersionCombo() {
    var combo = new ComboBox<String>();
    combo.setEditable(false);
    combo.setPromptText("Select tag");
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
    refreshTagsBtn.setTooltip(new Tooltip(AppConstants.REFRESH_TAGS_TOOLTIP));
    refreshTagsBtn.setOnAction(e -> loadTags(selectedProjectSupplier.get()));

    var box = new HBox(5, versionCombo, refreshTagsBtn);
    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    HBox.setHgrow(versionCombo, Priority.ALWAYS);
    return box;
  }

  private TextField createHashField() {
    var field = new TextField();
    field.setPromptText("Git hash (auto-filled from tag)");
    field.setEditable(false);
    return field;
  }
}

