package ee.aleksale.releaseapp.ui.components.form;

import jakarta.annotation.PostConstruct;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaveButtonComponent {

  @Getter
  private Button button;

  @PostConstruct
  void init() {
    button = new Button("Save Release");
    button.setMaxWidth(Double.MAX_VALUE);
    button.getStyleClass().add("primary-button");
  }

  public void setOnSave(Runnable saveAction) {
    button.setOnAction(event -> {
      if (saveAction != null) {
        saveAction.run();
      }
    });
  }
}

