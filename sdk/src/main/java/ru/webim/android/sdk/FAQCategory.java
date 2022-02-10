package ru.webim.android.sdk;

import java.util.List;

public interface FAQCategory {
    /**
     * @return category id
     */
    String getId();

    /**
     * @return category title
     */
    String getTitle();

    /**
     *
     * @return list of category items.
     */
    List<FAQItem> getItems();

    /**
     *
     * @return list of subcategories
     */
    List<FAQCategoryInfo> getSubCategories();
}
