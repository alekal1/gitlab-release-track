package ee.aleksale.releaseapp.ui.components.form;

import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import ee.aleksale.releaseapp.model.dto.response.jira.JiraIssuesResponse;
import ee.aleksale.releaseapp.service.JiraIssuesService;
import ee.aleksale.releaseapp.utils.AppConstants;
import ee.aleksale.releaseapp.utils.AsyncUtils;
import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueChooseComponent {

  @Getter
  private VBox content;

  private ComboBox<JiraIssuesResponse.JiraIssue> issueCombo;
  private ListView<JiraIssuesResponse.JiraIssue> selectedIssuesList;
  private ObservableList<JiraIssuesResponse.JiraIssue> selectedIssues;
  private ProgressIndicator issuesLoadingIndicator;

  private final JiraIssuesService jiraIssuesService;
  private final ApplicationEventPublisher eventPublisher;

  @PostConstruct
  void init() {
    issueCombo = createIssueCombo();
    selectedIssues = FXCollections.observableArrayList();
    selectedIssuesList = createSelectedIssuesList();

    issuesLoadingIndicator = new ProgressIndicator();
    issuesLoadingIndicator.setPrefSize(18, 18);
    issuesLoadingIndicator.setVisible(false);
    issuesLoadingIndicator.setManaged(false);

    content = new VBox(5,
            new Label("Issues:"),
            createIssuesBox()
    );

    initIssues();
  }

  public String buildIssuesValue() {
    return selectedIssues.stream()
            .map(JiraIssuesResponse.JiraIssue::formatIssue)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .collect(Collectors.joining(", "));
  }

  public void clearSelection() {
    issueCombo.setValue(null);
    selectedIssues.clear();
    selectedIssuesList.getSelectionModel().clearSelection();
  }

  private ComboBox<JiraIssuesResponse.JiraIssue> createIssueCombo() {
    var combo = new ComboBox<JiraIssuesResponse.JiraIssue>();
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setPromptText("Select Jira issue");
    combo.setCellFactory(lv -> JiraIssuesResponse.JiraIssue.issueCell());
    combo.setButtonCell(JiraIssuesResponse.JiraIssue.issueCell());
    return combo;
  }

  private ListView<JiraIssuesResponse.JiraIssue> createSelectedIssuesList() {
    var listView = new ListView<JiraIssuesResponse.JiraIssue>();
    listView.setItems(selectedIssues);
    listView.setCellFactory(lv -> JiraIssuesResponse.JiraIssue.issueCell());
    listView.setPrefHeight(90);
    return listView;
  }

  private VBox createIssuesBox() {
    var addIssueBtn = new Button(AppConstants.ADD_ICON);
    addIssueBtn.setOnAction(e -> addSelectedIssue());
    addIssueBtn.setMinWidth(32);
    addIssueBtn.setPrefWidth(32);

    var removeIssueBtn = new Button(AppConstants.DELETE_ICON);
    removeIssueBtn.setOnAction(e -> removeSelectedIssue());

    var refreshIssuesBtn = new Button(AppConstants.REFRESH_ICON);
    refreshIssuesBtn.setTooltip(new Tooltip(AppConstants.REFRESH_JIRA_TOOLTIP));
    refreshIssuesBtn.setOnAction(e -> initIssues());

    var pickerRow = new HBox(5, issueCombo, addIssueBtn, refreshIssuesBtn, issuesLoadingIndicator);
    pickerRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(issueCombo, Priority.ALWAYS);

    return new VBox(5, pickerRow, selectedIssuesList, removeIssueBtn);
  }

  private void initIssues() {
    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Loading Jira issues..."));
    issueCombo.setDisable(true);
    issueCombo.getItems().clear();
    issueCombo.setValue(null);
    toggleIssuesLoading(true);

    AsyncUtils.platformRunLater(jiraIssuesService::getBoardIssuesOnColumn, this::onIssuesLoaded);
  }

  private void onIssuesLoaded(List<JiraIssuesResponse.JiraIssue> issues) {
    toggleIssuesLoading(false);
    issueCombo.setDisable(false);

    if (issues == null || issues.isEmpty()) {
      eventPublisher.publishEvent(new StatusUpdateEvent(this, "No Jira issues found"));
      return;
    }

    issueCombo.getItems().setAll(issues);
    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Loaded " + issues.size() + " Jira issues"));
  }

  private void toggleIssuesLoading(boolean loading) {
    issuesLoadingIndicator.setVisible(loading);
    issuesLoadingIndicator.setManaged(loading);
  }

  private void addSelectedIssue() {
    var issue = issueCombo.getValue();
    if (issue == null || StringUtils.isBlank(issue.getKey())) {
      return;
    }

    var alreadySelected = selectedIssues.stream()
            .anyMatch(selected -> issue.getKey().equals(selected.getKey()));
    if (alreadySelected) {
      return;
    }

    selectedIssues.add(issue);
    issueCombo.setValue(null);
  }

  private void removeSelectedIssue() {
    var selectedIssue = selectedIssuesList.getSelectionModel().getSelectedItem();
    if (selectedIssue == null) {
      return;
    }
    selectedIssues.remove(selectedIssue);
  }
}

