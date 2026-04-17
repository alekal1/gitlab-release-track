package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.ReleaseDeletedEvent;
import ee.aleksale.releaseapp.event.ReleaseSavedEvent;
import ee.aleksale.releaseapp.service.ReleaseService;
import ee.aleksale.releaseapp.utils.AppConstants;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import lombok.Getter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class ReleaseDatePicker {

  @Getter
  private final DatePicker datePicker;
  @Getter
  private Set<LocalDate> datesWithReleases;

  private final ReleaseService releaseService;

  public ReleaseDatePicker(ReleaseService releaseService) {
    this.releaseService = releaseService;

    datePicker = new DatePicker(LocalDate.now());
    datePicker.setPrefWidth(AppConstants.DATE_PICKER_WIDTH);
    refreshCalendarHighlights();
  }

  public void refreshCalendarHighlights() {
    datesWithReleases = releaseService.getReleaseDates();
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

  @EventListener
  public void onReleaseSaved(ReleaseSavedEvent event) {
    refreshCalendarHighlights();
  }

  @EventListener
  public void onReleaseDelete(ReleaseDeletedEvent event) {
    refreshCalendarHighlights();
  }

}
