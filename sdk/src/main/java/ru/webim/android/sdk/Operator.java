package ru.webim.android.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Abstracts a chat operator
 * @see MessageStream#getCurrentOperator()
 */
public interface Operator {

    /**
     * @return the unique id of the operator
     * @see MessageStream#rateOperator
     * @see MessageStream#getLastOperatorRating
     */
    @NonNull Operator.Id getId();

    /**
     * @return a display name of the operator
     */
    @NonNull String getName();

    /**
     * @return a URL of the operator’s avatar
     */
    @Nullable String getAvatarUrl();

    /**
     * @return operator's title.
     */
    @Nullable String getTitle();

    /**
     * @return operator's additional information.
     */
    @Nullable String getInfo();

    /**
     * Abstracts the unique id of the operator. The class was designed only to be compared to 'equals'.
     */
    interface Id {
    }
}
