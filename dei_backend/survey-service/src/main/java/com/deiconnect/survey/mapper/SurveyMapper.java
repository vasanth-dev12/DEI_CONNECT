package com.deiconnect.survey.mapper;

import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.dto.SurveyResponse;
import com.deiconnect.survey.entity.InclusionSurvey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SurveyMapper {

    private final SurveyQuestionMapper questionMapper;

    public SurveyResponse toResponseWithQuestions(InclusionSurvey survey) {
        List<QuestionResponse> questions = survey.getQuestions().stream()
                .map(questionMapper::toResponse)
                .toList();
        return build(survey, questions, null);
    }

    public SurveyResponse toResponse(InclusionSurvey survey) {
        return build(survey, null, null);
    }

    public SurveyResponse toResponse(InclusionSurvey survey, List<QuestionResponse> questions) {
        return build(survey, questions, null);
    }

    public SurveyResponse toResponse(InclusionSurvey survey, Boolean respondedByMe) {
        return build(survey, null, respondedByMe);
    }

    public SurveyResponse toResponse(InclusionSurvey survey, List<QuestionResponse> questions,
                                     Boolean respondedByMe) {
        return build(survey, questions, respondedByMe);
    }

    private SurveyResponse build(InclusionSurvey survey, List<QuestionResponse> questions,
                                 Boolean respondedByMe) {
        return new SurveyResponse(
                survey.getId(),
                survey.getSurveyName(),
                survey.getSurveyType(),
                survey.getLaunchDate(),
                survey.getCloseDate(),
                survey.getAnonymised(),
                survey.getMinResponseThreshold(),
                survey.getStatus(),
                survey.getCreatorManagerId(),
                respondedByMe,
                questions);
    }
}
