package ru.webim.android.sdk;

/**
 * Created by Nikita Lazarev-Zubov on 13.12.17
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URL;
import java.util.Map;

/**
 * Single department entity. Provides methods to get department information.
 * Department objects can be received through
 * {@link MessageStream.DepartmentListChangeListener} methods
 * and {@link MessageStream#getDepartmentList()}.
 */
public interface Department {
    /**
     * Department key is used to start chat with some department.
     * @see MessageStream#startChatWithDepartmentKey(String)
     * @return department key value that uniquely identifies this department
     */
    @NonNull
    String getKey();

    /**
     * @return department public name
     */
    @NonNull
    String getName();

    /**
     * @see DepartmentOnlineStatus
     * @return department online status
     */
    @NonNull
    DepartmentOnlineStatus getDepartmentOnlineStatus();

    /**
     * @return order number (higher numbers match higher priority)
     */
    int getOrder();

    /**
     * @return map of department localized names if exists
     * (key is custom locale descriptor, value is matching name)
     */
    @Nullable
    Map<String, String> getLocalizedNames();

    /**
     * @return department logo URL (if exists)
     */
    @Nullable
    URL getLogoUrl();

    /**
     * Possible department online statuses.
     * @see Department#getDepartmentOnlineStatus()
     */
    enum DepartmentOnlineStatus {
        /**
         * Offline state with chats' count limit exceeded.
         */
        BUSY_OFFLINE,
        /**
         * Online state with chats' count limit exceeded.
         */
        BUSY_ONLINE,
        /**
         * Visitor is able to send offline messages.
         */
        OFFLINE,
        /**
         * Visitor is able to send both online and offline messages.
         */
        ONLINE,
        /**
         * Any status that is not supported by this version of the library.
         */
        UNKNOWN
    }
}
