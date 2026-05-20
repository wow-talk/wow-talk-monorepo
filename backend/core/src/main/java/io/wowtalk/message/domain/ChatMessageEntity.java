package io.wowtalk.message.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected ChatMessageEntity() {
    }

    public ChatMessageEntity(String roomId, String sessionId, String payload, MessageStatus status, Instant sentAt) {
        this.roomId = roomId;
        this.sessionId = sessionId;
        this.payload = payload;
        this.status = status;
        this.sentAt = sentAt;
    }

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = MessageStatus.ACTIVE;
        }
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }
}
