package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Abstracts a survey.
 */
public interface Survey {

    /**
     * @return config of the survey
     */
    @NonNull Config getConfig();

    /**
     * @return information of the current question
     */
    @NonNull CurrentQuestionInfo getCurrentQuestionInfo();

    /**
     * @return current survey id
     */
    @NonNull String getId();

    /**
     * @see Survey#getConfig()
     */
    interface Config {

        /**
         * @return config id
         */
        int getId();

        /**
         * @return descriptor of the current survey
         */
        @NonNull Descriptor getDescriptor();

        /**
         * @return descriptor version
         */
        @NonNull String getVersion();
    }

    /**
     * @see Config#getDescriptor()
     */
    interface Descriptor {

        /**
         * @return list of forms
         */
        @NonNull List<Form> getForms();
    }

    /**
     * @see Descriptor#getForms()
     */
    interface Form {

        /**
         * @return id of form
         */
        int getId();

        /**
         * @return list of questions
         */
        @NonNull List<Question> getQuestions();
    }

    /**
     * @see Form#getQuestions()
     */
    interface Question {

        /**
         * @return type of the question
         */
        @NonNull Type getType();

        /**
         * @return text of the question
         */
        @NonNull String getText();

        /**
         * @return options for radio question type
         */
        @Nullable List<String> getOptions();

        /**
         * Show the types of the question.
         * @see Question#getType()
         */
        enum Type {

            /**
             * User need to rate.
             */
            STARS,

            /**
             * User need to choose the option.
             */
            RADIO,

            /**
             * User need to write comment.
             */
            COMMENT
        }
    }

    /**
     * @see Survey#getCurrentQuestionInfo()
     */
    interface CurrentQuestionInfo {

        /**
         * @return form id of the current question
         */
        int getFormId();

        /**
         * @return question id of the current question
         */
        int getQuestionId();
    }
}
