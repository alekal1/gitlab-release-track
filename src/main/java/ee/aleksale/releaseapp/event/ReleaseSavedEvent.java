package ee.aleksale.releaseapp.event;

import ee.aleksale.releaseapp.model.dto.Release;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReleaseSavedEvent extends ApplicationEvent {

    private final Release release;

    public ReleaseSavedEvent(Object source, Release release) {
        super(source);
        this.release = release;
    }
}


