package com.deiconnect.goal.repository;

import com.deiconnect.goal.entity.DEIGoal;
import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DEIGoalRepository extends JpaRepository<DEIGoal, Long> {

    @Query("select g from DEIGoal g where "
            + "(:dimension is null or g.dimension = :dimension) and "
            + "(:status is null or g.status = :status) and "
            + "(:ownerId is null or g.owner.id = :ownerId) and "
            + "(:creatorManagerId is null or g.creatorManagerId = :creatorManagerId)")
    Page<DEIGoal> search(@Param("dimension") GoalDimension dimension,
                         @Param("status") GoalStatus status,
                         @Param("ownerId") Long ownerId,
                         @Param("creatorManagerId") Long creatorManagerId,
                         Pageable pageable);

    long countByStatus(GoalStatus status);

    long countByStatusAndOwner_DepartmentId(GoalStatus status, Long departmentId);

    long countByOwner_DepartmentId(Long departmentId);

    long countByStatusAndOwner_GradeId(GoalStatus status, Long gradeId);

    long countByOwner_GradeId(Long gradeId);

    long countByStatusAndOwner_HrId(GoalStatus status, Long hrId);

    long countByOwner_HrId(Long hrId);

    long countByStatusAndOwner_HrIdAndOwner_DepartmentId(GoalStatus status, Long hrId, Long departmentId);

    long countByOwner_HrIdAndOwner_DepartmentId(Long hrId, Long departmentId);

    long countByStatusAndOwner_HrIdAndOwner_GradeId(GoalStatus status, Long hrId, Long gradeId);

    long countByOwner_HrIdAndOwner_GradeId(Long hrId, Long gradeId);
}
