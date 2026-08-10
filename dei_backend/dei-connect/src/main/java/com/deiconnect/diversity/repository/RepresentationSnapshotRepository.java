package com.deiconnect.diversity.repository;

import com.deiconnect.diversity.entity.RepresentationSnapshot;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RepresentationSnapshotRepository extends JpaRepository<RepresentationSnapshot, Long> {

    @Query("select s from RepresentationSnapshot s where "
            + "(:dimension is null or s.dimension = :dimension) and "
            + "(:departmentId is null or s.departmentId = :departmentId) and "
            + "(:status is null or s.status = :status) and "
            + "(:creatorManagerId is null or s.creatorManagerId = :creatorManagerId)")
    Page<RepresentationSnapshot> search(@Param("dimension") DemographicDimension dimension,
                                        @Param("departmentId") Long departmentId,
                                        @Param("status") SnapshotStatus status,
                                        @Param("creatorManagerId") Long creatorManagerId,
                                        Pageable pageable);

    @Query("select s from RepresentationSnapshot s where "
            + "(:dimension is null or s.dimension = :dimension) and "
            + "(:departmentId is null or s.departmentId = :departmentId) and "
            + "(:status is null or s.status = :status) and "
            + "(:creatorManagerId is null or s.creatorManagerId = :creatorManagerId) "
            + "order by s.snapshotDate desc, s.dimension asc, s.departmentId asc, s.groupName asc")
    List<RepresentationSnapshot> findAllForRuns(@Param("dimension") DemographicDimension dimension,
                                                @Param("departmentId") Long departmentId,
                                                @Param("status") SnapshotStatus status,
                                                @Param("creatorManagerId") Long creatorManagerId);

    @Query("select s from RepresentationSnapshot s where "
            + "s.snapshotDate = :snapshotDate and "
            + "s.dimension = :dimension and "
            + "((:departmentId is null and s.departmentId is null) or s.departmentId = :departmentId) and "
            + "((:creatorManagerId is null and s.creatorManagerId is null) or s.creatorManagerId = :creatorManagerId) "
            + "order by s.groupName asc")
    List<RepresentationSnapshot> findDistribution(@Param("snapshotDate") LocalDate snapshotDate,
                                                  @Param("dimension") DemographicDimension dimension,
                                                  @Param("departmentId") Long departmentId,
                                                  @Param("creatorManagerId") Long creatorManagerId);
}
