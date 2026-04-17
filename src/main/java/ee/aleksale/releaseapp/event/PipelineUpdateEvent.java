package ee.aleksale.releaseapp.event;

import ee.aleksale.releaseapp.model.dto.Release;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PipelineUpdateEvent extends ApplicationEvent {

  private final Release release;

  public PipelineUpdateEvent(Object source, Release release) {
    super(source);
    this.release = release;
  }
}
