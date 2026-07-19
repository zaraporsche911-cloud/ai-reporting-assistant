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
@Table(name = "saved_reports")
public class SavedReport extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generated_report_id", nullable = false)
    private GeneratedReport generatedReport;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String tags;

    @Column(nullable = false)
    private boolean favorite;

    @Column(nullable = false)
    private boolean pinned;

    @Column(name = "shared_internally", nullable = false)
    private boolean sharedInternally;

    protected SavedReport() {
    }

    public SavedReport(AppUser user, GeneratedReport generatedReport, String title, String description, String tags) {
        this.user = user;
        this.generatedReport = generatedReport;
        updateMetadata(title, description, tags, false, false, false);
    }

    public void updateMetadata(
            String title,
            String description,
            String tags,
            boolean favorite,
            boolean pinned,
            boolean sharedInternally
    ) {
        this.title = DomainText.require(title, "title", 160);
        this.description = DomainText.optional(description, 1000);
        this.tags = DomainText.optional(tags, 500);
        this.favorite = favorite;
        this.pinned = pinned;
        this.sharedInternally = sharedInternally;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public GeneratedReport getGeneratedReport() { return generatedReport; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTags() { return tags; }
    public boolean isFavorite() { return favorite; }
    public boolean isPinned() { return pinned; }
    public boolean isSharedInternally() { return sharedInternally; }
}
