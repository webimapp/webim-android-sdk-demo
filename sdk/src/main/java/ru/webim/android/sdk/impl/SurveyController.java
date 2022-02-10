package ru.webim.android.sdk.impl;

import ru.webim.android.sdk.MessageStream;
import ru.webim.android.sdk.Survey;

import java.util.List;

public class SurveyController {
    private MessageStream.SurveyListener surveyListener;
    private Survey survey;
    private int currentFormPointer = 0;
    private int currentQuestionPointer = 0;

    public SurveyController(MessageStream.SurveyListener surveyListener) {
        this.surveyListener = surveyListener;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
        setCurrentQuestionPointer();
        surveyListener.onSurvey(survey);
    }

    public Survey getSurvey() {
        return survey;
    }

    public int getCurrentFormId() {
        Survey.Form form = survey.getConfig().getDescriptor().getForms().get(currentFormPointer);
        return form.getId();
    }

    public int getCurrentQuestionPointer() {
        return currentQuestionPointer;
    }

    public void nextQuestion() {
        Survey.Question question = getCurrentQuestion();
        if (question != null) {
            surveyListener.onNextQuestion(question);
        }
    }

    public void cancelSurvey() {
        surveyListener.onSurveyCancelled();
    }

    private Survey.Question getCurrentQuestion() {
        List<Survey.Form> formsList = survey.getConfig().getDescriptor().getForms();
        if (formsList.size() <= currentFormPointer) {
            return null;
        }
        Survey.Form form = formsList.get(currentFormPointer);

        List<Survey.Question> questions = form.getQuestions();
        currentQuestionPointer++;
        if (questions.size() <= currentQuestionPointer) {
            currentQuestionPointer = -1;
            currentFormPointer++;
            return getCurrentQuestion();
        }
        return questions.get(currentQuestionPointer);
    }

    void deleteSurvey() {
        survey = null;
        currentFormPointer = 0;
        currentQuestionPointer = 0;
    }

    private void setCurrentQuestionPointer() {
        List<Survey.Form> formsList = survey.getConfig().getDescriptor().getForms();
        Survey.CurrentQuestionInfo questionInfo = survey.getCurrentQuestionInfo();
        currentQuestionPointer = questionInfo.getQuestionId() - 1;

        for (int i = 0; i < formsList.size(); i++) {
            Survey.Form form = formsList.get(i);
            if (form.getId() == questionInfo.getFormId()) {
                currentFormPointer = i;
                break;
            }
        }
    }
}
