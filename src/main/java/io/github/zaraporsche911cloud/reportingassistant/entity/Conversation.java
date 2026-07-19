package io.github.zaraporsche911cloud.reportingassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversations")
public class Conversation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 160)
    private String title;

    protected Conversation() {
    }

    public Conversation(AppUser user, String title) {
        this.user = user;
        this.title = DomainText.require(title, "title", 160);
    }

    public void rename(String title) {
        this.title = DomainText.require(title, "title", 160);
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public String getTitle() { return title; }
}
