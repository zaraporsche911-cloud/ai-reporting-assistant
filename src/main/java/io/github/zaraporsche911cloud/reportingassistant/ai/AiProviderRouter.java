package io.github.zaraporsche911cloud.reportingassistant.ai;

import io.github.zaraporsche911cloud.reportingassistant.config.AiProperties;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppSetting;
import io.github.zaraporsche911cloud.reportingassistant.exception.AiProviderException;
import io.github.zaraporsche911cloud.reportingassistant.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AiProviderRouter {

    public static final String ACTIVE_PROVIDER_KEY = "active-ai-provider";

    private final Map<String, AiProvider> providers;
    private final AppSettingRepository settings;
    private final AiProperties properties;

    public AiProviderRouter(List<AiProvider> providers, AppSettingRepository settings, AiProperties properties) {
        this.providers = new LinkedHashMap<>();
        providers.forEach(provider -> this.providers.put(provider.id(), provider));
        this.settings = settings;
        this.properties = properties;
    }

    public AiProvider active() {
        String id = activeId();
        AiProvider provider = providers.get(id);
        if (provider == null) throw new AiProviderException("Unknown AI provider: " + id);
        AiProvider.ProviderHealth health = provider.health();
        if (!health.available()) throw new AiProviderException(health.message());
        return provider;
    }

    public String activeId() {
        String configured = settings.findById(ACTIVE_PROVIDER_KEY).map(AppSetting::getValue).orElse("DEFAULT");
        return "DEFAULT".equalsIgnoreCase(configured)
                ? properties.provider()
                : configured.toLowerCase(Locale.ROOT);
    }

    public List<ProviderStatus> statuses() {
        String active = activeId();
        return providers.values().stream().map(provider -> {
            AiProvider.ProviderHealth health = provider.health();
            return new ProviderStatus(provider.id(), provider.id().equals(active), health.available(), health.message(), health.model());
        }).toList();
    }

    @Transactional
    public ProviderStatus changeActive(String providerId) {
        String normalized = providerId == null ? "" : providerId.trim().toLowerCase(Locale.ROOT);
        AiProvider provider = providers.get(normalized);
        if (provider == null) throw new IllegalArgumentException("Unknown AI provider: " + providerId);
        AiProvider.ProviderHealth health = provider.health();
        if (!health.available()) throw new AiProviderException(health.message());
        AppSetting setting = settings.findById(ACTIVE_PROVIDER_KEY)
                .orElseGet(() -> new AppSetting(ACTIVE_PROVIDER_KEY, normalized));
        setting.changeValue(normalized);
        settings.save(setting);
        return new ProviderStatus(normalized, true, true, health.message(), health.model());
    }

    public record ProviderStatus(String id, boolean active, boolean available, String message, String model) {
    }
}
