package io.wowtalk.postgres.room;

import io.wowtalk.room.domain.RoomMemberRole;
import io.wowtalk.room.domain.RoomMemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "room_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_room_members_room_user", columnNames = {"room_id", "user_id"})
)
public class RoomMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private RoomMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoomMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected RoomMemberEntity() {
    }

    public RoomMemberEntity(String roomId, String userId, RoomMemberRole role, RoomMemberStatus status, Instant joinedAt) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    @PrePersist
    void prePersist() {
        if (role == null) {
            role = RoomMemberRole.MEMBER;
        }
        if (status == null) {
            status = RoomMemberStatus.ACTIVE;
        }
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
