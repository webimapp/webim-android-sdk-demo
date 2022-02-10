package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SurveyItem {
    @SerializedName("config")
    private Config config;
    @SerializedName("current_question")
    private CurrentQuestionInfo currentQuestionInfo;
    @SerializedName("id")
    private String id;

    public Config getConfig() {
        return config;
    }

    public CurrentQuestionInfo getCurrentQuestionInfo() {
        return currentQuestionInfo;
    }

    public String getId() {
        return id;
    }

    public static final class Config {
        @SerializedName("id")
        private int id;
        @SerializedName("descriptor")
        private Descriptor descriptor;
        @SerializedName("version")
        private String version;

        public int getId() {
            return id;
        }

        public Descriptor getDescriptor() {
            return descriptor;
        }

        public String getVersion() {
            return version;
        }
    }

    public static final class Descriptor {
        @SerializedName("forms")
        private List<Form> forms;

        public List<Form> getForms() {
            return forms;
        }
    }

    public static final class Form {
        @SerializedName("id")
        private int id;
        @SerializedName("questions")
        private List<Question> questions;

        public int getId() {
            return id;
        }

        public List<Question> getQuestions() {
            return questions;
        }
    }

    public static final class Question {
        @SerializedName("type")
        private Type type;
        @SerializedName("text")
        private String text;
        @SerializedName("options")
        private List<String> options;

        public Type getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public List<String> getOptions() {
            return options;
        }

        public enum Type {
            @SerializedName("stars")
            STARS,
            @SerializedName("radio")
            RADIO,
            @SerializedName("comment")
            COMMENT
        }
    }

    public static final class CurrentQuestionInfo {
        @SerializedName("form_id")
        private int formId;
        @SerializedName("question_id")
        private int questionId;

        public int getFormId() {
            return formId;
        }

        public int getQuestionId() {
            return questionId;
        }
    }
}
