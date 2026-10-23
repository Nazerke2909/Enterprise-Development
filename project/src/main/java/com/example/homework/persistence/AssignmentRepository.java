package com.example.homework.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    Optional<AssignmentEntity> findByBusinessKey(String businessKey);

    long countByBusinessKey(String businessKey);

    List<AssignmentEntity> findAllByOrderByCreatedAtAscBusinessKeyAsc();
}
