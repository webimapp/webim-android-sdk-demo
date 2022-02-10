package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.Survey;
import ru.webim.android.sdk.impl.items.SurveyItem;

import java.util.ArrayList;
import java.util.List;

class SurveyFactory {

    @Nullable
    Survey createSurvey(@NonNull SurveyItem surveyItem) {
        return new SurveyImpl(
            createConfig(surveyItem.getConfig()),
            createCurrentQuestionInfo(surveyItem.getCurrentQuestionInfo()),
            surveyItem.getId()
        );
    }

    private Survey.Config createConfig(SurveyItem.Config config) {
        SurveyItem.Descriptor descriptor = config.getDescriptor();
        return new SurveyImpl.ConfigImpl(
            config.getId(),
            createDescriptor(descriptor),
            config.getVersion()
        );
    }

    private Survey.Descriptor createDescriptor(SurveyItem.Descriptor descriptor) {
        List<Survey.Form> formsList = new ArrayList<>();
        for (SurveyItem.Form form : descriptor.getForms()) {
            Survey.Form newForm = new SurveyImpl.FormImpl(
                form.getId(),
                createQuestions(form.getQuestions())
            );
            formsList.add(newForm);
        }
        return new SurveyImpl.DescriptorImpl(formsList);
    }

    private List<Survey.Question> createQuestions(List<SurveyItem.Question> questions) {
        List<Survey.Question> questionsList = new ArrayList<>();
        for (SurveyItem.Question question : questions) {
            Survey.Question newQuestion = new SurveyImpl.QuestionImpl(
                InternalUtils.getQuestionType(question.getType()),
                question.getText(),
                question.getOptions()
            );
            questionsList.add(newQuestion);
        }
        return questionsList;
    }

    private Survey.CurrentQuestionInfo createCurrentQuestionInfo(
        SurveyItem.CurrentQuestionInfo currentQuestionInfo) {
        return new SurveyImpl.CurrentQuestionInfoImpl(
            currentQuestionInfo.getFormId(), currentQuestionInfo.getQuestionId());
    }
}
