package io.github.zaraporsche911cloud.reportingassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MessageAuthor author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "generated_report_id")
    private Long generatedReportId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Message() {
    }

    public Message(Conversation conversation, MessageAuthor author, String content, Long generatedReportId) {
        this.conversation = conversation;
        this.author = author;
        this.content = DomainText.require(content, "content", 10_000);
        this.generatedReportId = generatedReportId;
    }

    public Long getId() { return id; }
    public Conversation getConversation() { return conversation; }
    public MessageAuthor getAuthor() { return author; }
    public String getContent() { return content; }
    public Long getGeneratedReportId() { return generatedReportId; }
    public Instant getCreatedAt() { return createdAt; }
}
