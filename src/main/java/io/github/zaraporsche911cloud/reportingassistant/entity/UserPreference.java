package io.github.zaraporsche911cloud.reportingassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_preferences", uniqueConstraints = @UniqueConstraint(name = "uk_user_preferences_user", columnNames = "user_id"))
public class UserPreference extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "dark_mode", nullable = false)
    private boolean darkMode;

    @Column(nullable = false, length = 64)
    private String timezone = "UTC";

    protected UserPreference() {
    }

    public UserPreference(AppUser user) {
        this.user = user;
    }

    public void update(boolean darkMode, String timezone) {
        this.darkMode = darkMode;
        this.timezone = DomainText.require(timezone, "timezone", 64);
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public boolean isDarkMode() { return darkMode; }
    public String getTimezone() { return timezone; }
}
