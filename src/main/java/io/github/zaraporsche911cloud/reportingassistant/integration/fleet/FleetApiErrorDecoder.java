package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import io.github.zaraporsche911cloud.reportingassistant.exception.FleetIntegrationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FleetApiErrorDecoder {

    public FleetIntegrationException decode(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            return new FleetIntegrationException(
                    "Fleet Control Tower request failed with status " + responseException.getStatusCode().value());
        }
        return new FleetIntegrationException("Fleet Control Tower is unavailable", exception);
    }
}
