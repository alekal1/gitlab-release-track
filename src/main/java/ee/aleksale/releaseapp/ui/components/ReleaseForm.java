package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import ee.aleksale.releaseapp.model.common.PipelineStatus;
import ee.aleksale.releaseapp.model.common.PipelineType;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.ui.components.form.IssueChooseComponent;
import ee.aleksale.releaseapp.ui.components.form.ProjectSelectionComponent;
import ee.aleksale.releaseapp.ui.components.form.SaveButtonComponent;
import ee.aleksale.releaseapp.ui.components.form.TitleComponent;
import ee.aleksale.releaseapp.ui.components.form.VersionSelectionComponent;
import ee.aleksale.releaseapp.utils.AppConstants;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleaseForm {

  @Getter
  private VBox form;

  private final ReleaseDatePicker releaseDatePicker;

  private final TitleComponent titleComponent;
  private final ProjectSelectionComponent projectSelectionComponent;
  private final VersionSelectionComponent versionSelectionComponent;
  private final IssueChooseComponent issueChooseComponent;
  private final SaveButtonComponent saveButtonComponent;
  private final ApplicationEventPublisher eventPublisher;

  @PostConstruct
  void initForm() {
    form = new VBox(8);
    form.setPadding(new Insets(10));
    form.setPrefWidth(AppConstants.FORM_WIDTH);
    form.getStyleClass().add("form-panel");

    projectSelectionComponent.setOnProjectSelected(project -> {
      if (project == null) {
        versionSelectionComponent.clearAvailableTags();
        return;
      }
      versionSelectionComponent.loadTags(project);
    });
    versionSelectionComponent.setSelectedProject(projectSelectionComponent::getSelectedProject);
    saveButtonComponent.setOnSave(this::createAndSaveRelease);

    form.getChildren().addAll(
            titleComponent.getContent(),
            projectSelectionComponent.getContent(),
            new Separator(),
            versionSelectionComponent.getContent(),
            new Separator(),
            issueChooseComponent.getContent(),
            new Separator(),
            saveButtonComponent.getButton()
    );
  }

  private void createAndSaveRelease() {
    var project = projectSelectionComponent.getSelectedProject();
    if (project == null) {
      return;
    }

    var version = versionSelectionComponent.getVersionText();
    if (StringUtils.isBlank(version)) {
      return;
    }

    var release = Release.builder()
            .gitlabProjectName(project.getName())
            .version(version)
            .gitHash(versionSelectionComponent.getGitHash())
            .pipelineType(PipelineType.UNKNOWN)
            .pipelineStatus(PipelineStatus.PENDING)
            .issues(issueChooseComponent.buildIssuesValue())
            .releaseDate(getSelectedDate())
            .build();
    clearForm();
    eventPublisher.publishEvent(new ReleaseSavedEvent(this, release));
    eventPublisher.publishEvent(new StatusUpdateEvent(this,
            String.format("Release saved: %s %s for %s", project.getName(), version, getSelectedDate())));
  }

  private void clearForm() {
    versionSelectionComponent.clearSelection();
    issueChooseComponent.clearSelection();
  }

  private LocalDate getSelectedDate() {
    var datePicker = releaseDatePicker.getDatePicker();
    return datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
  }
}
