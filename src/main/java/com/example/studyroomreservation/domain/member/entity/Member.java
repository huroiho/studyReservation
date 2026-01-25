package com.example.studyroomreservation.domain.member.entity;

import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.global.common.BaseSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member extends BaseSoftDeletableEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    private Member(String name, String email, String encodedPassword, String phoneNumber, Role role) {
        this.name = name;
        this.email = email;
        this.password = encodedPassword;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public static Member createUser(String name, String email, String encodedPassword, String phoneNumber) {
        return new Member(name, email, encodedPassword, phoneNumber, Role.USER);
    }

    public static Member createAdmin(String name, String email, String encodedPassword, String phoneNumber) {
        return new Member(name, email, encodedPassword, phoneNumber, Role.ADMIN);
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
