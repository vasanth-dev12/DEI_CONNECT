package com.deiconnect.survey.service;

import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.dto.UpdateQuestionRequest;

import java.util.List;

public interface SurveyQuestionService {

    List<QuestionResponse> listForSurvey(Long surveyId, Long managerIdFilter);

    QuestionResponse add(Long surveyId, CreateQuestionRequest request);

    QuestionResponse update(Long surveyId, Long questionId, UpdateQuestionRequest request);

    void delete(Long surveyId, Long questionId);
}
