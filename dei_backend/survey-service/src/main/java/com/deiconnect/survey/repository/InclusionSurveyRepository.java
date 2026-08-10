package com.deiconnect.survey.repository;

import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InclusionSurveyRepository extends JpaRepository<InclusionSurvey, Long> {

    Page<InclusionSurvey> findByStatus(SurveyStatus status, Pageable pageable);

    Page<InclusionSurvey> findByStatusAndCreatorManagerId(SurveyStatus status, Long creatorManagerId, Pageable pageable);

    Page<InclusionSurvey> findByCreatorManagerId(Long creatorManagerId, Pageable pageable);

    @Query("select s from InclusionSurvey s "
            + "where s.creatorManagerId is null or s.creatorManagerId = :managerId")
    Page<InclusionSurvey> findVisibleToManager(@Param("managerId") Long managerId, Pageable pageable);

    @Query("select s from InclusionSurvey s "
            + "where s.status = :status and (s.creatorManagerId is null or s.creatorManagerId = :managerId)")
    Page<InclusionSurvey> findVisibleToManagerByStatus(@Param("status") SurveyStatus status,
                                                       @Param("managerId") Long managerId,
                                                       Pageable pageable);

    @Query("select distinct s from InclusionSurvey s join s.questions q "
            + "where s.status = :status "
            + "and (s.creatorManagerId is null or s.creatorManagerId = :managerId) "
            + "and (q.creatorManagerId is null or q.creatorManagerId = :managerId)")
    Page<InclusionSurvey> findAnswerableByEmployee(@Param("status") SurveyStatus status,
                                                  @Param("managerId") Long managerId,
                                                  Pageable pageable);
}
