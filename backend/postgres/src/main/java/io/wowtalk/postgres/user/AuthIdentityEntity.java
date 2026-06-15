package io.wowtalk.postgres.user;

import io.wowtalk.user.domain.AuthIdentityId;
import io.wowtalk.user.domain.AuthProvider;
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
        name = "auth_identities",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_auth_identity_provider_subject",
                columnNames = {"provider", "provider_subject"}
        )
)
public class AuthIdentityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_identity_id", nullable = false, unique = true, length = 100)
    private String authIdentityId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private AuthProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 200)
    private String providerSubject;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuthIdentityEntity() {
    }

    public AuthIdentityEntity(String authIdentityId, String userId, AuthProvider provider, String providerSubject, Instant createdAt) {
        this.authIdentityId = authIdentityId;
        this.userId = userId;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (authIdentityId == null || authIdentityId.isBlank()) {
            authIdentityId = AuthIdentityId.newId().value();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
