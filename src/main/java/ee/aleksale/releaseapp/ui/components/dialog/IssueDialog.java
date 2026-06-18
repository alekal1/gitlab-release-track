package ee.aleksale.releaseapp.ui.components.dialog;

import static ee.aleksale.releaseapp.utils.BrowserUtils.openInBrowser;

import ee.aleksale.releaseapp.config.JiraConfig;
import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.utils.IssueUtils;
import ee.aleksale.releaseapp.utils.AppConstants;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueDialog {

  private final JiraConfig jiraConfig;
  private final ApplicationEventPublisher eventPublisher;

  public Optional<IssueUtils.IssueEntry> showIssueDialog(Release release) {
    var issues = IssueUtils.parseIssues(release.getIssues());

    if (issues.isEmpty()) {
      eventPublisher.publishEvent(new StatusUpdateEvent(this, "No issues attached to this release"));
      return Optional.empty();
    }

    var dialog = new Dialog<IssueUtils.IssueEntry>();
    dialog.setTitle("Release issues");
    dialog.setHeaderText("Click issue to open in Jira");
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    var listView = new ListView<>(FXCollections.observableArrayList(issues));
    listView.setPrefHeight(AppConstants.PROJECT_CHOOSE_DIALOG_HEIGHT);
    listView.setPrefWidth(AppConstants.PROJECT_CHOOSE_DIALOG_WIDTH);
    listView.setCellFactory(ignored -> new ListCell<>() {
      @Override
      protected void updateItem(IssueUtils.IssueEntry item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.display());
      }
    });
    listView.setOnMouseClicked(event -> {
      var selected = listView.getSelectionModel().getSelectedItem();
      if (selected != null) {
        openIssueInBrowser(selected.key());
      }
    });

    dialog.getDialogPane().setContent(listView);
    return dialog.showAndWait();
  }

  private void openIssueInBrowser(String issueKey) {
    if (issueKey == null || issueKey.isBlank()) {
      return;
    }

    var jiraBaseUrl = jiraConfig.getBaseUrl();
    if (jiraBaseUrl == null || jiraBaseUrl.isBlank()) {
      return;
    }

    openInBrowser(jiraBaseUrl + "/browse/" + issueKey.trim());
  }
}
