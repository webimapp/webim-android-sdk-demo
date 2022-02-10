package ru.webim.android.sdk.impl.items;

public enum VisitSessionStateItem {
    CALLBACK_HUNTER("callback-hunter"),
    CHAT("chat"),
    CHAT_SHOWING("chat-showing"),
    DEPARTMENT_SELECTION("department-selection"),
    END("end"),
    IDLE("idle"),
    IDLE_AFTER_CHAT("idle-after-chat"),
    FIRST_QUESTION("first-question"),
    OFFLINE_MESSAGE("offline-message"),
    SHOWING("showing"),
    SHOWING_AUTO("showing-auto"),
    SHOWING_BY_URL_PARAM("showing-by-url-param"),
    UNKNOWN("_unknown");

    private String typeValue;

    VisitSessionStateItem(String type) {
        typeValue = type;
    }

    static public VisitSessionStateItem getType(String pType) {
        for (VisitSessionStateItem type : VisitSessionStateItem.values()) {
            if (type.getTypeValue().equals(pType)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getTypeValue() {
        return typeValue;
    }

}
