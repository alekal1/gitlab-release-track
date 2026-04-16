package ee.aleksale.releaseapp;

import ee.aleksale.releaseapp.stage.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

  private ConfigurableApplicationContext springContext;

  @Override
  public void start(Stage primaryStage) {
    springContext.publishEvent(new StageReadyEvent(primaryStage));
  }

  @Override
  public void init() {
    springContext = new SpringApplicationBuilder(GitlabReleaseTrackApplication.class)
            .headless(false)
            .run(getParameters().getRaw().toArray(new String[0]));
  }

  @Override
  public void stop() {
    springContext.stop();
    Platform.exit();
  }
}
