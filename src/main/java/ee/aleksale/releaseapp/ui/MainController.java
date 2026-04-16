package ee.aleksale.releaseapp.ui;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainController {

  public Parent build() {
    return new BorderPane();
  }
}
