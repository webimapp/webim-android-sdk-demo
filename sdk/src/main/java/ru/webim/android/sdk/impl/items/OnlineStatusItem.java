package ru.webim.android.sdk.impl.items;

/**
 * <p>- UNKNOWN("unknown"), - session has not received data from server; </p>
 * <p>- ONLINE("online"), - user can send online and offline messages; </p>
 * <p>- BUSY_ONLINE("busy_online"), - user can send offline messages, but server can return error; </p>
 * <p>- OFFLINE("offline"), - user can send offline messages; </p>
 * <p>- BUSY_OFFLINE("busy_offline") - user can't send messages; </p>
 */
public enum OnlineStatusItem {
    UNKNOWN("unknown"),
    ONLINE("online"),
    BUSY_ONLINE("busy_online"),
    OFFLINE("offline"),
    BUSY_OFFLINE("busy_offline");

    private String typeValue;

    OnlineStatusItem(String type) {
        typeValue = type;
    }

    static public OnlineStatusItem getType(String pType) {
        for (OnlineStatusItem type : OnlineStatusItem.values()) {
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
