package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.utils.AppConstants;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;

public class ReleaseDatePicker {

  @Getter
  private final DatePicker datePicker;
  @Getter
  private Set<LocalDate> datesWithReleases;

  public ReleaseDatePicker(LocalDate localDate) {
    datePicker = new DatePicker(localDate);
    datePicker.setPrefWidth(AppConstants.DATE_PICKER_WIDTH);
  }

  public void refreshCalendarHighlights(Set<LocalDate> dates) {
    datesWithReleases = dates;
    datePicker.setDayCellFactory(createHighlightedDayCellFactory());
  }


  private Callback<DatePicker, DateCell> createHighlightedDayCellFactory() {
    return dp -> new DateCell() {
      @Override
      public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        if (!empty && date != null && datesWithReleases.contains(date)) {
          setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");
          setTooltip(new Tooltip("Has releases"));
        }
      }
    };
  }

}
