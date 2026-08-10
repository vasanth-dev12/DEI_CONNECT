package com.deiconnect.erg.repository;

import com.deiconnect.erg.entity.ERGMembership;
import com.deiconnect.erg.enums.MembershipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ErgMembershipRepository extends JpaRepository<ERGMembership, Long> {

    Optional<ERGMembership> findByErg_IdAndEmployeeUserId(Long ergId, Long employeeUserId);

    Page<ERGMembership> findByErg_Id(Long ergId, Pageable pageable);

    Page<ERGMembership> findByEmployeeUserId(Long employeeUserId, Pageable pageable);

    int countByErg_IdAndStatus(Long ergId, MembershipStatus status);

    @Query("select distinct m.employeeUserId from ERGMembership m where m.status = com.deiconnect.erg.enums.MembershipStatus.ACTIVE")
    java.util.List<Long> findActiveMemberUserIds();
}
