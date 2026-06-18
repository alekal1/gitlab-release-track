package ee.aleksale.releaseapp.ui.components.form;

import jakarta.annotation.PostConstruct;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TitleComponent {

  @Getter
  private Label content;

  @PostConstruct
  void init() {
    content = new Label("New Release");
    content.getStyleClass().add("form-title");
  }
}
