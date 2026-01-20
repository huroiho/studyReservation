package com.example.studyroomreservation.domain.member.repository;

import com.example.studyroomreservation.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
