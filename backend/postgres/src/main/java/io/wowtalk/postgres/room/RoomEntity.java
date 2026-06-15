package io.wowtalk.postgres.room;

import io.wowtalk.room.domain.RoomStatus;
import io.wowtalk.room.domain.RoomType;
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
@Table(name = "rooms")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, unique = true, length = 100)
    private String roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoomStatus status;

    @Column(name = "max_members", nullable = false)
    private int maxMembers;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RoomEntity() {
    }

    public RoomEntity(String roomId, RoomType roomType, RoomStatus status, int maxMembers, Instant createdAt) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.status = status;
        this.maxMembers = maxMembers;
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
