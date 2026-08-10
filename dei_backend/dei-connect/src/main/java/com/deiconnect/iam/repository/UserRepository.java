package com.deiconnect.iam.repository;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByManager_IdAndRole(Long managerId, Role role, Pageable pageable);

    List<User> findByManager_IdAndRoleAndStatus(Long managerId, Role role, UserStatus status);

    Page<User> findByHr_IdAndRole(Long hrId, Role role, Pageable pageable);

    long countByStatus(UserStatus status);

    long countByStatusAndDepartmentId(UserStatus status, Long departmentId);

    long countByStatusAndGradeId(UserStatus status, Long gradeId);

    long countByStatusAndHr_Id(UserStatus status, Long hrId);

    long countByStatusAndHr_IdAndDepartmentId(UserStatus status, Long hrId, Long departmentId);

    long countByStatusAndHr_IdAndGradeId(UserStatus status, Long hrId, Long gradeId);

    long countByStatusAndHr_IdAndDepartmentIdAndGradeId(UserStatus status, Long hrId, Long departmentId, Long gradeId);

    @Query("select distinct u.departmentId, u.departmentName from User u where u.departmentId is not null order by u.departmentId")
    List<Object[]> findDistinctDepartments();

    @Query("select distinct u.gradeId from User u where u.gradeId is not null order by u.gradeId")
    List<Long> findDistinctGrades();
}
