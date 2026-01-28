package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationPolicyRepository extends JpaRepository<OperationPolicy, Long> {

    boolean existsByName(String name);
}
