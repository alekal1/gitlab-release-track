package ee.aleksale.releaseapp.ui.components.dialog;

import ee.aleksale.releaseapp.model.dto.GitlabProject;
import ee.aleksale.releaseapp.utils.AppConstants;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectSearchResultDialog {

  public Optional<GitlabProject> showSearchResultDialog(List<GitlabProject> results) {
    var dialog = new Dialog<GitlabProject>();
    dialog.setTitle(AppConstants.PROJECT_CHOOSE_DIALOG_TITLE);
    dialog.setHeaderText(AppConstants.PROJECT_CHOOSE_HEADER_TEXT);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    ListView<GitlabProject> listView = new ListView<>(FXCollections.observableArrayList(results));
    listView.setCellFactory(lv -> GitlabProject.projectCell(project -> project.getName() + "  [ID: " + project.getGitlabProjectId() + "]"));
    listView.setPrefHeight(AppConstants.PROJECT_CHOOSE_DIALOG_HEIGHT);
    listView.setPrefWidth(AppConstants.PROJECT_CHOOSE_DIALOG_WIDTH);

    dialog.getDialogPane().setContent(listView);
    dialog.setResultConverter(btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedItem() : null);

    return dialog.showAndWait();
  }


}
