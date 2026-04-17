package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.service.ReleaseService;
import ee.aleksale.releaseapp.utils.AppConstants;
import ee.aleksale.releaseapp.utils.DateUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReleasesTable {

  @Setter
  private ObservableList<Release> releaseData;
  @Getter
  private TableView<Release> table;

  private final ReleaseService releaseService;

  public ReleasesTable(ReleaseService releaseService) {
    this.releaseService = releaseService;
    releaseData = FXCollections.observableArrayList();
    createTable();
  }

  public void refreshTable(LocalDate date) {
    final var releases = releaseService.getReleasesByDateAndService(date);
    releaseData.setAll(releases);
  }

  @EventListener
  public void onReleaseSaved(ReleaseSavedEvent event) {
    Platform.runLater(() -> refreshTable(event.getRelease().getReleaseDate()));
  }

  private void createTable() {
    table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setItems(releaseData);

    table.getColumns().addAll(createTableColumns());
  }

  private List<TableColumn<Release, String>> createTableColumns() {
    var projectNameCol = new TableColumn<Release, String>("Project");
    projectNameCol.setCellValueFactory(new PropertyValueFactory<>("gitlabProjectName"));

    var versionCol = new TableColumn<Release, String>("Version");
    versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

    var hashCol = new TableColumn<Release, String>("Git Hash");
    hashCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getGitHash() != null && c.getValue().getGitHash().length() > 8
                    ? c.getValue().getGitHash().substring(0, 8)
                    : c.getValue().getGitHash()));

    var pipelineCol = new TableColumn<Release, String>("Pipeline");
    pipelineCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPipelineType().name()));

    var statusCol = new TableColumn<Release, String>("Status");
    statusCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getPipelineStatus() != null ? c.getValue().getPipelineStatus().name() : ""));
    statusCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        if (!empty && item != null) {
          switch (item) {
            case AppConstants.PIPELINE_STATUS_SUCCESS ->  setStyle("-fx-text-fill: #4caf50;");
            case AppConstants.PIPELINE_STATUS_FAILED -> setStyle("-fx-text-fill: #f44336;");
            case AppConstants.PIPELINE_STATUS_RUNNING -> setStyle("-fx-text-fill: #ff9800;");
            default -> setStyle("-fx-text-fill: #90caf9;");
          }
        } else {
          setStyle("");
        }
      }
    });

    var notesCol = new TableColumn<Release, String>("Notes");
    notesCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));

    var dateCol = new TableColumn<Release, String>("Created");
    dateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCreatedAt().format(DateUtils.FMT)));

    return List.of(
            projectNameCol, versionCol, hashCol, pipelineCol, statusCol, notesCol, dateCol
    );
  }
}
