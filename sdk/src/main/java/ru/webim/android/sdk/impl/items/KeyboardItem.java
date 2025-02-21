package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class KeyboardItem {
    @SerializedName("buttons")
    private List<List<Button>> buttons;
    @SerializedName("state")
    private State state;
    @SerializedName("response")
    private Response response;

    public State getState() {
        return state;
    }

    public List<List<Button>> getButtons() {
        return buttons;
    }

    public Response getResponse() {
        return response;
    }

    public static final class Button {
        @SerializedName("id")
        private String id;
        @SerializedName("text")
        private String text;
        @SerializedName("config")
        private Configuration configuration;
        @SerializedName("params")
        private Params params;

        public String getId(){
            return id;
        }

        public String getText() {
            return text;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public Params getParams() {
            return params;
        }

        public static final class Configuration {
            @SerializedName("text_to_insert")
            private String textToInsert;
            @SerializedName("link")
            private String link;
            @SerializedName("state")
            private State state;

            public String getTextToInsert() {
                return textToInsert;
            }

            public String getLink() {
                return link;
            }

            public State getState() {
                return state;
            }

            public enum State {
                @SerializedName("showing")
                SHOWING,
                @SerializedName("showing_selected")
                SHOWING_SELECTED,
                @SerializedName("hidden")
                HIDDEN,
            }
        }

        public static final class Params {
            @SerializedName("type")
            private Type type;
            @SerializedName("action")
            private String action;
            @SerializedName("color")
            private String color;

            public Type getType() {
                return type;
            }

            public String getAction() {
                return action;
            }

            public String getColor() {
                return color;
            }

            public enum Type {
                @SerializedName("url")
                URL,
                @SerializedName("action")
                ACTION
            }
        }
    }

    public final class Response {
        @SerializedName("buttonId")
        private String buttonId;
        @SerializedName("messageId")
        private String messageId;

        public String getButtonId() {
            return buttonId;
        }

        public String getMessageId() {
            return messageId;
        }
    }

    public enum State {
        @SerializedName("pending")
        PENDING,
        @SerializedName("cancelled")
        CANCELLED,
        @SerializedName("completed")
        COMPLETED,
    }
}
