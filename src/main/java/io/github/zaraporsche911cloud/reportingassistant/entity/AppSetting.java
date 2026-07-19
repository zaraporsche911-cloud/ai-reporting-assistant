package io.github.zaraporsche911cloud.reportingassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_settings")
public class AppSetting extends AuditableEntity {

    @Id
    @Column(name = "setting_key", length = 64)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String value;

    protected AppSetting() {
    }

    public AppSetting(String key, String value) {
        this.key = DomainText.require(key, "key", 64);
        this.value = DomainText.require(value, "value", 500);
    }

    public void changeValue(String value) {
        this.value = DomainText.require(value, "value", 500);
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
}
