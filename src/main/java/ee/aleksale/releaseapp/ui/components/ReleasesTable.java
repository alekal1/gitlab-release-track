package ee.aleksale.releaseapp.ui.components;

import static java.awt.Desktop.getDesktop;

import ee.aleksale.releaseapp.event.PipelineUpdateEvent;
import ee.aleksale.releaseapp.event.ReleaseDeletedEvent;
import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import ee.aleksale.releaseapp.model.dto.Release;
import ee.aleksale.releaseapp.service.ReleaseService;
import ee.aleksale.releaseapp.utils.AppConstants;
import ee.aleksale.releaseapp.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleasesTable {

  private ObservableList<Release> releaseData;
  @Getter
  private TableView<Release> table;

  private final ReleaseService releaseService;
  private final ApplicationEventPublisher eventPublisher;


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
            createPipelineActionColumn(),
            createStatusColumn(),
            createNotesColumn(),
            createDateColumn(),
            openCommitButtonColumn(),
            deleteButtonColumn()
    );
  }

  public void refreshTable(LocalDate date) {
    releaseData.setAll(releaseService.getReleasesByDate(date));
  }

  @EventListener
  private void onReleaseSaved(ReleaseSavedEvent event) {
    Platform.runLater(() -> refreshTable(event.getRelease().getReleaseDate()));
  }

  @EventListener
  private void onPipelineUpdate(PipelineUpdateEvent event) {
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
      return new SimpleStringProperty(hash);
    });
    return col;
  }

  private TableColumn<Release, String> createPipelineActionColumn() {
    var col = new TableColumn<Release, String>("Pipeline action");
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

  private TableColumn<Release, Void> openCommitButtonColumn() {
    var col = new TableColumn<Release, Void>("");
    col.setPrefWidth(40);
    col.setMinWidth(40);
    col.setMaxWidth(40);
    col.setCellFactory(c -> new TableCell<>() {
      private final Button btn = new Button("🔗");
      {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #90caf9; -fx-cursor: hand; -fx-font-size: 11;");
        btn.setTooltip(new Tooltip("Open commit in GitLab"));
        btn.setOnAction(e -> {
          Release release = getTableView().getItems().get(getIndex());
          String webUrl = release.getGitlabProjectWebUrl();
          String hash = release.getGitHash();
          if (webUrl != null && hash != null) {
            openInBrowser(webUrl + "/-/commit/" + hash);
          }
        });
      }
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    });
    return col;
  }

  private TableColumn<Release, Void> deleteButtonColumn() {
    var col = new TableColumn<Release, Void>("");
    col.setPrefWidth(40);
    col.setMinWidth(40);
    col.setMaxWidth(40);
    col.setCellFactory(c -> new TableCell<>() {
      private final Button btn = new Button(AppConstants.DELETE_ICON);
      {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-cursor: hand; -fx-font-size: 11;");
        btn.setTooltip(new Tooltip("Delete release"));
        btn.setOnAction(e -> {
          Release release = getTableView().getItems().get(getIndex());
          Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                  "Delete " + release.getGitlabProjectName() + " " + release.getVersion() + "?",
                  ButtonType.YES, ButtonType.NO);
          confirm.setHeaderText("Confirm Delete");
          confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
              Platform.runLater(() -> refreshTable(release.getReleaseDate()));
              eventPublisher.publishEvent(new ReleaseDeletedEvent(this, release));
              eventPublisher.publishEvent(new StatusUpdateEvent(this, "Deleted: "
                      + release.getGitlabProjectName() + " " + release.getVersion()));
            }
          });
        });
      }
      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
      }
    });
    return col;
  }

  private void openInBrowser(String url) {
    try {
      getDesktop().browse(new URI(url));
    } catch (Exception e) {
      log.error("Failed to open browser for URL: {}", url, e);
    }
  }
}
