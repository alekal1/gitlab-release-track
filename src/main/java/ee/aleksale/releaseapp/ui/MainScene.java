package ee.aleksale.releaseapp.ui;

import ee.aleksale.releaseapp.ui.components.ReleaseDatePicker;
import ee.aleksale.releaseapp.ui.components.ReleaseForm;
import ee.aleksale.releaseapp.ui.components.ReleaseTopBox;
import ee.aleksale.releaseapp.ui.components.ReleasesTable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainScene {

  private final ReleaseDatePicker releaseDatePicker;

  private final ReleaseTopBox releaseTopBox;
  private final ReleasesTable releasesTable;
  private final ReleaseForm releaseForm;

  public Parent build() {
    var root = new BorderPane();
    root.getStyleClass().add("root-pane");

    root.setTop(releaseTopBox.getTop());
    root.setCenter(releasesTable.getTable());
    root.setRight(releaseForm.getForm());

    releasesTable.refreshTable(releaseDatePicker.getDatePicker().getValue());

    return root;
  }
}
