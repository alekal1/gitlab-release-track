package ee.aleksale.releaseapp.model.common;

import lombok.Getter;

public enum PipelineStatus {
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    SUCCESS("SUCCESS"),
    MANUALLY_SUCCESS("MANUALLY_SUCCESS"),
    FAILED("FAILED");

    @Getter
    final String val;

    PipelineStatus(final String val) {
        this.val = val;
    }
}

