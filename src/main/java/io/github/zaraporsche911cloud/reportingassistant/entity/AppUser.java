package io.github.zaraporsche911cloud.reportingassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name = "uk_reporting_users_email", columnNames = "email"))
public class AppUser extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    protected AppUser() {
    }

    public AppUser(String fullName, String email, String passwordHash, UserRole role) {
        this.fullName = DomainText.require(fullName, "fullName", 120);
        this.email = DomainText.require(email, "email", 254).toLowerCase(Locale.ROOT);
        this.passwordHash = DomainText.require(passwordHash, "passwordHash", 100);
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    public void updateProfile(String fullName) {
        this.fullName = DomainText.require(fullName, "fullName", 120);
    }

    public void changeRole(UserRole role) {
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public boolean isEnabled() { return enabled; }
}
