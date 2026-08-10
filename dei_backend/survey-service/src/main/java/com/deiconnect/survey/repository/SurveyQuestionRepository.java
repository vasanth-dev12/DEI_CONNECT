package com.deiconnect.survey.repository;

import com.deiconnect.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findBySurvey_IdOrderBySequenceOrderAsc(Long surveyId);

    List<SurveyQuestion> findBySurvey_IdAndCreatorManagerIdOrderBySequenceOrderAsc(Long surveyId, Long creatorManagerId);

    @Query("select q from SurveyQuestion q "
            + "where q.survey.id = :surveyId "
            + "and (q.creatorManagerId is null or q.creatorManagerId = :managerId) "
            + "order by q.sequenceOrder asc")
    List<SurveyQuestion> findVisibleQuestions(@Param("surveyId") Long surveyId,
                                              @Param("managerId") Long managerId);

    @Query("select max(q.sequenceOrder) from SurveyQuestion q where q.survey.id = :surveyId")
    Integer findMaxSequenceOrder(@Param("surveyId") Long surveyId);
}
