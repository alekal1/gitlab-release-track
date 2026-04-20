package ee.aleksale.releaseapp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StatusUpdateEvent extends ApplicationEvent {

  private final String status;

  public StatusUpdateEvent(Object source, String status) {
    super(source);
    this.status = status;
  }
}
