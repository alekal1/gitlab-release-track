package ee.aleksale.releaseapp.utils;

import javafx.application.Platform;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class AsyncUtils {

  public static <T> void platformRunLater(Supplier<T> backgroundTask, Consumer<T> callback) {
    Thread.ofVirtual().start(() -> {
      try {
        var result = backgroundTask.get();
        Platform.runLater(() -> callback.accept(result));
      } catch (Exception e) {
        log.error("Async task failed", e);
      }
    });
  }
}
