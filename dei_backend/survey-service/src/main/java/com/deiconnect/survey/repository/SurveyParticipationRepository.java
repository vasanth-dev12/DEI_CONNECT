package com.deiconnect.survey.repository;

import com.deiconnect.survey.entity.SurveyParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, Long> {

    boolean existsBySurvey_IdAndEmployeeUserId(Long surveyId, Long employeeUserId);
}
