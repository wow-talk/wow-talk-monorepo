package io.wowtalk.channel.domain;

import io.wowtalk.transport.TransportMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "channels")
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, unique = true, length = 100)
    private String roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, length = 30)
    private TransportMode transportMode;

    protected ChannelEntity() {
    }

    public ChannelEntity(String roomId, TransportMode transportMode) {
        this.roomId = roomId;
        this.transportMode = transportMode;
    }
}
