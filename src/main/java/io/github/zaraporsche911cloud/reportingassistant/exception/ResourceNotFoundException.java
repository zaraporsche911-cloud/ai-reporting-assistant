package io.github.zaraporsche911cloud.reportingassistant.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " was not found: " + id);
    }
}
