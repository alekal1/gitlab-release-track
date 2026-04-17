package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.service.ReleaseService;
import ee.aleksale.releaseapp.utils.AppConstants;
import ee.aleksale.releaseapp.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReleasesTable {

  private static final int GIT_HASH_DISPLAY_LENGTH = 8;

  private ObservableList<Release> releaseData;
  @Getter
  private TableView<Release> table;

  private final ReleaseService releaseService;

  @SuppressWarnings("unchecked")
  @PostConstruct
  void initTable() {
    releaseData = FXCollections.observableArrayList();
    table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setItems(releaseData);
    table.getColumns().addAll(
            createProjectColumn(),
            createVersionColumn(),
            createHashColumn(),
            createPipelineColumn(),
            createStatusColumn(),
            createNotesColumn(),
            createDateColumn()
    );
  }

  public void refreshTable(LocalDate date) {
    releaseData.setAll(releaseService.getReleasesByDateAndService(date));
  }

  @EventListener
  public void onReleaseSaved(ReleaseSavedEvent event) {
    Platform.runLater(() -> refreshTable(event.getRelease().getReleaseDate()));
  }

  private TableColumn<Release, String> createProjectColumn() {
    var col = new TableColumn<Release, String>("Project");
    col.setCellValueFactory(new PropertyValueFactory<>("gitlabProjectName"));
    return col;
  }

  private TableColumn<Release, String> createVersionColumn() {
    var col = new TableColumn<Release, String>("Version");
    col.setCellValueFactory(new PropertyValueFactory<>("version"));
    return col;
  }

  private TableColumn<Release, String> createHashColumn() {
    var col = new TableColumn<Release, String>("Git Hash");
    col.setCellValueFactory(c -> {
      var hash = c.getValue().getGitHash();
      var display = hash != null && hash.length() > GIT_HASH_DISPLAY_LENGTH
              ? hash.substring(0, GIT_HASH_DISPLAY_LENGTH)
              : hash;
      return new SimpleStringProperty(display);
    });
    return col;
  }

  private TableColumn<Release, String> createPipelineColumn() {
    var col = new TableColumn<Release, String>("Pipeline");
    col.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPipelineType().name()));
    return col;
  }

  private TableColumn<Release, String> createStatusColumn() {
    var col = new TableColumn<Release, String>("Status");
    col.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getPipelineStatus() != null ? c.getValue().getPipelineStatus().name() : ""));
    col.setCellFactory(ignored -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        if (!empty && item != null) {
          setStyle(switch (item) {
            case AppConstants.PIPELINE_STATUS_SUCCESS -> "-fx-text-fill: #4caf50;";
            case AppConstants.PIPELINE_STATUS_FAILED -> "-fx-text-fill: #f44336;";
            case AppConstants.PIPELINE_STATUS_RUNNING -> "-fx-text-fill: #ff9800;";
            default -> "-fx-text-fill: #90caf9;";
          });
        } else {
          setStyle("");
        }
      }
    });
    return col;
  }

  private TableColumn<Release, String> createNotesColumn() {
    var col = new TableColumn<Release, String>("Notes");
    col.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));
    return col;
  }

  private TableColumn<Release, String> createDateColumn() {
    var col = new TableColumn<Release, String>("Created");
    col.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCreatedAt().format(DateUtils.FMT)));
    return col;
  }
}
