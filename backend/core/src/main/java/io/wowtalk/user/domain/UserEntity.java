package io.wowtalk.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 30)
    private UserType userType;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    protected UserEntity() {
    }

    public UserEntity(String userId, UserType userType, String displayName) {
        this.userId = userId;
        this.userType = userType;
        this.displayName = displayName;
    }

    @PrePersist
    void prePersist() {
        if (userId == null || userId.isBlank()) {
            userId = UserId.newId().value();
        }
    }
}
