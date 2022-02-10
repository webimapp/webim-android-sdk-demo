package ru.webim.android.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.Survey;

import java.util.List;

class SurveyImpl implements Survey {
    private Config config;
    private CurrentQuestionInfo currentQuestionInfo;
    private String id;

    public SurveyImpl(Config config, CurrentQuestionInfo currentQuestionInfo, String id) {
        this.config = config;
        this.currentQuestionInfo = currentQuestionInfo;
        this.id = id;
    }

    @NonNull
    @Override
    public Config getConfig() {
        return config;
    }

    @NonNull
    @Override
    public CurrentQuestionInfo getCurrentQuestionInfo() {
        return currentQuestionInfo;
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    public static class ConfigImpl implements Config {
        private int id;
        private Descriptor descriptor;
        private String version;

        public ConfigImpl(int id, Descriptor descriptor, String version) {
            this.id = id;
            this.descriptor = descriptor;
            this.version = version;
        }

        @Override
        public int getId() {
            return id;
        }

        @NonNull
        @Override
        public Descriptor getDescriptor() {
            return descriptor;
        }

        @NonNull
        @Override
        public String getVersion() {
            return version;
        }
    }

    public static class DescriptorImpl implements Descriptor {
        private List<Form> forms;

        public DescriptorImpl(List<Form> forms) {
            this.forms = forms;
        }

        @NonNull
        @Override
        public List<Form> getForms() {
            return forms;
        }
    }

    public static class FormImpl implements Form {
        private int id;
        private List<Question> questions;

        public FormImpl(int id, List<Question> questions) {
            this.id = id;
            this.questions = questions;
        }

        @Override
        public int getId() {
            return id;
        }

        @NonNull
        @Override
        public List<Question> getQuestions() {
            return questions;
        }
    }

    public static class QuestionImpl implements Question {
        private Type type;
        private String text;
        private List<String> options;

        public QuestionImpl(Type type, String text, List<String> options) {
            this.type = type;
            this.text = text;
            this.options = options;
        }

        @NonNull
        @Override
        public Type getType() {
            return type;
        }

        @NonNull
        @Override
        public String getText() {
            return text;
        }

        @Nullable
        @Override
        public List<String> getOptions() {
            return options;
        }
    }

    public static class CurrentQuestionInfoImpl implements CurrentQuestionInfo {
        private int formId;
        private int questionId;

        public CurrentQuestionInfoImpl(int formId, int questionId) {
            this.formId = formId;
            this.questionId = questionId;
        }

        @Override
        public int getFormId() {
            return formId;
        }

        @Override
        public int getQuestionId() {
            return questionId;
        }
    }
}
