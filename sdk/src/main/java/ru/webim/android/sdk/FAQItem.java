package ru.webim.android.sdk;

import java.util.List;

public interface FAQItem {
    /**
     * @return item id
     */
    String getId();

    /**
     * @return list of categories that contains the item
     */
    List<String> getCategories();

    /**
     * @return item title
     */
    String getTitle();

    /**
     * @return list of item tags
     */
    List<String> getTags();

    /**
     * @return item content
     */
    String getContent();

    /**
     * @return like count of the item
     */
    int getLikeCount();

    /**
     * @return dislike count of the item
     */
    int getDislikeCount();

    /**
     * @return user rate of the item
     */
    UserRate getUserRate();

    enum UserRate {
        /**
         * Item is liked by user.
         */
        LIKE,
        /**
         * Item is disliked by user.
         */
        DISLIKE,
        /**
         * User doesn't rate the item.
         */
        NO_RATE
    }
}
