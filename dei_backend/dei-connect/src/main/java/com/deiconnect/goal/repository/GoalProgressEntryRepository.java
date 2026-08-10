package com.deiconnect.goal.repository;

import com.deiconnect.goal.entity.GoalProgressEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalProgressEntryRepository extends JpaRepository<GoalProgressEntry, Long> {

    Page<GoalProgressEntry> findByGoal_Id(Long goalId, Pageable pageable);

    Optional<GoalProgressEntry> findFirstByGoal_IdOrderByIdDesc(Long goalId);

    Optional<GoalProgressEntry> findFirstByGoal_IdAndIdLessThanOrderByIdDesc(Long goalId, Long id);
}
