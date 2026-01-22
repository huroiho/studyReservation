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
        this.activeUpdatedAt = LocalDateTime.now();
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