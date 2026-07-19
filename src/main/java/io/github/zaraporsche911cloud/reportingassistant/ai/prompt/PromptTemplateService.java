package io.github.zaraporsche911cloud.reportingassistant.ai.prompt;

import io.github.zaraporsche911cloud.reportingassistant.entity.PromptTemplate;
import io.github.zaraporsche911cloud.reportingassistant.exception.ResourceNotFoundException;
import io.github.zaraporsche911cloud.reportingassistant.repository.PromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PromptTemplateService {

    public static final String INTENT = "intent-extraction";
    public static final String SUMMARY = "report-summary";

    private final PromptTemplateRepository repository;

    public PromptTemplateService(PromptTemplateRepository repository) {
        this.repository = repository;
    }

    public String content(String key) {
        return repository.findByKeyAndEnabledTrue(key)
                .map(PromptTemplate::getContent)
                .orElseThrow(() -> new ResourceNotFoundException("Enabled prompt template", key));
    }

    public List<PromptTemplate> list() {
        return repository.findAll();
    }

    @Transactional
    public PromptTemplate update(Long id, String displayName, String content, boolean enabled) {
        PromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt template", id));
        template.update(displayName, content, enabled);
        return template;
    }
}
