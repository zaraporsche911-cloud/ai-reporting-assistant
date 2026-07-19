package io.github.zaraporsche911cloud.reportingassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "prompt_templates", uniqueConstraints = @UniqueConstraint(name = "uk_prompt_template_key", columnNames = "template_key"))
public class PromptTemplate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_key", nullable = false, length = 64)
    private String key;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean enabled = true;

    protected PromptTemplate() {
    }

    public PromptTemplate(String key, String displayName, String content) {
        this.key = DomainText.require(key, "key", 64);
        this.displayName = DomainText.require(displayName, "displayName", 120);
        this.content = DomainText.require(content, "content", 30_000);
    }

    public void update(String displayName, String content, boolean enabled) {
        this.displayName = DomainText.require(displayName, "displayName", 120);
        this.content = DomainText.require(content, "content", 30_000);
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public String getContent() { return content; }
    public boolean isEnabled() { return enabled; }
}
