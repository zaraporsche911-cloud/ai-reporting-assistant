package io.github.zaraporsche911cloud.reportingassistant.integration.fleet;

import io.github.zaraporsche911cloud.reportingassistant.config.FleetApiProperties;
import io.github.zaraporsche911cloud.reportingassistant.exception.FleetIntegrationException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Component
public class FleetApiTokenProvider {

    private final FleetApiProperties properties;
    private final RestClient loginClient;
    private final Clock clock;
    private volatile CachedToken cachedToken;

    public FleetApiTokenProvider(FleetApiProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        this.loginClient = RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
    }

    public String token() {
        if (properties.token() != null && !properties.token().isBlank()) {
            return properties.token().trim();
        }
        CachedToken current = cachedToken;
        if (current != null && current.expiresAt().isAfter(clock.instant().plusSeconds(30))) {
            return current.value();
        }
        return authenticate();
    }

    public synchronized void invalidate() {
        cachedToken = null;
    }

    private synchronized String authenticate() {
        CachedToken current = cachedToken;
        if (current != null && current.expiresAt().isAfter(clock.instant().plusSeconds(30))) {
            return current.value();
        }
        if (properties.username() == null || properties.username().isBlank()
                || properties.password() == null || properties.password().isBlank()) {
            throw new FleetIntegrationException(
                    "LIVE mode requires FLEET_API_TOKEN or FLEET_API_USERNAME and FLEET_API_PASSWORD");
        }
        try {
            LoginResponse response = loginClient.post()
                    .uri("/api/v1/auth/login")
                    .body(Map.of("email", properties.username(), "password", properties.password()))
                    .retrieve()
                    .body(LoginResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new FleetIntegrationException("Fleet Control Tower returned an invalid authentication response");
            }
            Instant expiresAt = response.expiresAt() == null ? clock.instant().plusSeconds(300) : response.expiresAt();
            cachedToken = new CachedToken(response.accessToken(), expiresAt);
            return response.accessToken();
        } catch (RestClientException exception) {
            throw new FleetIntegrationException("Unable to authenticate with Fleet Control Tower", exception);
        }
    }

    private record LoginResponse(String accessToken, Instant expiresAt) {
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
