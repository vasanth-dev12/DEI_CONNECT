package com.deiconnect.erg.repository;

import com.deiconnect.erg.entity.ERGEventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ERGEventParticipationRepository extends JpaRepository<ERGEventParticipation, Long> {
    boolean existsByEvent_IdAndEmployeeUserId(Long eventId, Long employeeUserId);
    Optional<ERGEventParticipation> findByEvent_IdAndEmployeeUserId(Long eventId, Long employeeUserId);
    List<ERGEventParticipation> findByEvent_Id(Long eventId);
}
