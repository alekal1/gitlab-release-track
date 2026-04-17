package ee.aleksale.releaseapp.ui;

import ee.aleksale.releaseapp.ui.components.ReleaseDatePicker;
import ee.aleksale.releaseapp.ui.components.ReleaseForm;
import ee.aleksale.releaseapp.ui.components.ReleasesTable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainController {

  private final ReleaseDatePicker releaseDatePicker;
  private final ReleasesTable releasesTable;
  private final ReleaseForm releaseForm;

  public Parent build() {
    var root = new BorderPane();
    root.getStyleClass().add("root-pane");

    root.setTop(createTopBar());
    root.setCenter(releasesTable.getTable());
    root.setRight(releaseForm.getForm());

    releasesTable.refreshTable(releaseDatePicker.getDatePicker().getValue());

    return root;
  }

  private HBox createTopBar() {
    var exportMdBtn = new Button("Export Markdown");
    // TODO: Export markdown functionality

    releaseDatePicker
            .getDatePicker()
            .valueProperty()
            .addListener((obs, o, n) ->
                    releasesTable.refreshTable(releaseDatePicker.getDatePicker().getValue())
    );

    var top = new HBox(10,
            new Label("Release Day:"), releaseDatePicker.getDatePicker(),
            new Separator(Orientation.VERTICAL),
            exportMdBtn);
    top.setAlignment(Pos.CENTER_LEFT);
    top.setPadding(new Insets(10));
    return top;
  }
}
