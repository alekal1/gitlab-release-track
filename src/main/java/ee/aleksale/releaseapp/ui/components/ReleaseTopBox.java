package ee.aleksale.releaseapp.ui.components;

import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleaseTopBox {

  @Getter
  private HBox top;

  private final ReleaseDatePicker releaseDatePicker;
  private final ReleasesTable releasesTable;

  @PostConstruct
  void initTop() {
    releaseDatePicker
            .getDatePicker()
            .valueProperty()
            .addListener((obs, o, n) ->
                    releasesTable.refreshTable(releaseDatePicker.getDatePicker().getValue())
            );

    top = new HBox(10,
            new Label("Release Day:"), releaseDatePicker.getDatePicker(),
            new Separator(Orientation.VERTICAL));
    top.setAlignment(Pos.CENTER_LEFT);
    top.setPadding(new Insets(10));
  }
}
