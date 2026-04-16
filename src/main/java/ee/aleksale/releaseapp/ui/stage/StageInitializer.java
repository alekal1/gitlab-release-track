package ee.aleksale.releaseapp.ui.stage;

import ee.aleksale.releaseapp.ui.MainController;
import ee.aleksale.releaseapp.utils.AppConstants;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private static final String STYLES_PATH = "/styles/dark-theme.css";

    private final MainController mainController;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        Scene scene = new Scene(mainController.build(), AppConstants.STAGE_WIDTH, AppConstants.STAGE_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(STYLES_PATH)).toExternalForm());
        stage.setTitle(AppConstants.STAGE_TITLE);
        stage.setScene(scene);
        stage.show();
    }
}

