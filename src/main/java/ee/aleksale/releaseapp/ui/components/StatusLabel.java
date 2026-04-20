package ee.aleksale.releaseapp.ui.components;

import ee.aleksale.releaseapp.event.StatusUpdateEvent;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusLabel {

  @Getter
  private Label statusLabel;

  @PostConstruct
  void initStatus() {
    statusLabel = new Label("Ready");
    statusLabel.setPadding(new Insets(5, 10, 5, 10));
  }

  @EventListener
  public void onStatusUpdate(StatusUpdateEvent event) {
    statusLabel.setText(event.getStatus());
  }

}
