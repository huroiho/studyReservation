package com.example.studyroomreservation.domain.refund.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refund_policy")
public class RefundPolicy extends BaseCreatedEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime activeUpdatedAt;

    // 정책 삭제 시 규칙도 자동 삭제
    @OneToMany(mappedBy = "refundPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefundRule> rules = new ArrayList<>();

    private RefundPolicy(String name) {
        this.name = name;
        this.isActive = true;
    }

    public static RefundPolicy create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("정책 이름은 필수입니다.");
        }
        return new RefundPolicy(name);
    }

    // 연관 관계 편의 매서드(규칙 생성되면 정책에도 반영되도록)
    public void addRule(RefundRule rule) {
        this.rules.add(rule);
    }

    public void deactivate() {
        this.isActive = false;
        this.activeUpdatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.activeUpdatedAt = LocalDateTime.now();
    }
}