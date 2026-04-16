package ee.aleksale.releaseapp;


import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GitlabReleaseTrackApplication {

  public static void main(String[] args) {
    Application.launch(JavaFxApplication.class);
  }
}