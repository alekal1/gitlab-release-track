package ee.aleksale.releaseapp;


import ee.aleksale.releaseapp.ui.JavaFxApplication;
import ee.aleksale.releaseapp.utils.EnvUtils;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GitlabReleaseTrackApplication {

  public static void main(String[] args) {
    EnvUtils.loadEnvFile();
    Application.launch(JavaFxApplication.class);
  }
}