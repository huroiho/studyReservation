package com.example.studyroomreservation.global.security.auth;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.entity.Role;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class MemberContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String email;
    private String name; // header 회원이름 노출용 주석제거
    //private String phone;
    private Role role;

    public MemberContext(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        //this.phone = member.getPhone();
        this.role = member.getRole();
    }
}
