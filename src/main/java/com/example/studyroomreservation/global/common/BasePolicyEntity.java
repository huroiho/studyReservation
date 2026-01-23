package com.example.studyroomreservation.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BasePolicyEntity extends BaseCreatedEntity{

    @Column(nullable = false, updatable = false)
    protected String name;

    @Column(nullable = false)
    protected boolean isActive = true;

    @Column(name = "active_updated_at", nullable = false)
    protected LocalDateTime activeUpdatedAt;

    protected void activate() {
        if (!this.isActive) {
            this.isActive = true;
            this.activeUpdatedAt = LocalDateTime.now();
        }
    }

    protected void deactivate() {
        if (this.isActive) {
            this.isActive = false;
            this.activeUpdatedAt = LocalDateTime.now();
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.activeUpdatedAt == null) {
            this.activeUpdatedAt = LocalDateTime.now();
        }
    }
}
