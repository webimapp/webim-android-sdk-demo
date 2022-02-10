package ru.webim.android.sdk;

import java.util.List;

public interface FAQStructure {
    /**
     * @return unique id of the tree root.
     */
    String getId();

    /**
     * @return root type
     */
    FAQType getType();

    /**
     * @return root's children
     */
    List<FAQStructure> getChildren();

    /**
     * @return root title
     */
    String getTitle();

    /**
     * Child type.
     */
    enum FAQType {
        /**
         * Item element.
         */
        ITEM,

        /**
         * Category element.
         */
        CATEGORY,

        /**
         * Unknown type.
         */
        UNKNOWN
    }
}
