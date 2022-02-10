package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Created by Nikita Lazarev-Zubov on 13.12.17
 */

public final class DepartmentItem {
    @SerializedName("key")
    private String key;
    @SerializedName("name")
    private String name;
    @SerializedName("online")
    private String onlineStatus;
    @SerializedName("order")
    private int order;
    @SerializedName("localeToName")
    private Map<String, String> localizedNames;
    @SerializedName("logo")
    private String logoUrlString;

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public InternalDepartmentOnlineStatus getOnlineStatus() {
        return InternalDepartmentOnlineStatus.getType(onlineStatus);
    }

    public int getOrder() {
        return order;
    }

    public Map<String, String> getLocalizedNames() {
        return localizedNames;
    }

    public String getLogoUrlString() {
        return logoUrlString;
    }

    public enum InternalDepartmentOnlineStatus {
        BUSY_OFFLINE("busy_offline"),
        BUSY_ONLINE("busy_online"),
        OFFLINE("offline"),
        ONLINE("online"),
        UNKNOWN("_unknown");

        private String typeValue;

        InternalDepartmentOnlineStatus(String typeValue) {
            this.typeValue = typeValue;
        }

        public static InternalDepartmentOnlineStatus getType(String typeValue) {
            for (InternalDepartmentOnlineStatus type : InternalDepartmentOnlineStatus.values()) {
                if (type.getTypeValue().equals(typeValue)) {
                    return type;
                }
            }

            return UNKNOWN;
        }

        private String getTypeValue() {
            return typeValue;
        }
    }
}
