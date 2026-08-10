package com.deiconnect.survey.mapper;

import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.entity.SurveyQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SurveyQuestionMapper {

    private final UserClient userClient;

    public SurveyQuestion toEntity(CreateQuestionRequest request) {
        return SurveyQuestion.builder()
                .questionText(request.questionText())
                .questionType(request.questionType())
                .dimension(request.dimension())
                .mandatory(request.mandatory())
                .sequenceOrder(request.sequenceOrder())
                .build();
    }

    public QuestionResponse toResponse(SurveyQuestion question) {
        String managerName = null;
        if (question.getCreatorManagerId() != null) {
            try {
                managerName = userClient.getByIdInternal(question.getCreatorManagerId()).name();
            } catch (Exception e) {
                managerName = "System User (Offline)";
            }
        }
        return new QuestionResponse(
                question.getId(),
                question.getSurvey() == null ? null : question.getSurvey().getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getDimension(),
                question.getMandatory(),
                question.getSequenceOrder(),
                question.getCreatorManagerId(),
                managerName);
    }
}
