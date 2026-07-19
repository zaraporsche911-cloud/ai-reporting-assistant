package io.github.zaraporsche911cloud.reportingassistant.exception;

public class FleetIntegrationException extends RuntimeException {
    public FleetIntegrationException(String message) {
        super(message);
    }

    public FleetIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
