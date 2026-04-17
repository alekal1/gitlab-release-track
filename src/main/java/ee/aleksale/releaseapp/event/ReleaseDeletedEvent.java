package ee.aleksale.releaseapp.event;

import ee.aleksale.releaseapp.model.dto.Release;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReleaseDeletedEvent extends ApplicationEvent {

  private final Release release;

  public ReleaseDeletedEvent(Object source, Release release) {
    super(source);
    this.release = release;
  }
}
