package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;
import ru.webim.android.sdk.FAQCategory;
import ru.webim.android.sdk.FAQCategoryInfo;
import ru.webim.android.sdk.FAQItem;

import java.util.ArrayList;
import java.util.List;

public class FAQCategoryItem implements FAQCategory {
    @SerializedName("categoryid")
    private String categoryId;
    @SerializedName("title")
    private String title;
    @SerializedName("childs")
    private List<ChildItem> childs;

    @Override
    public String getId() {
        return categoryId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<FAQItem> getItems() {
        List<FAQItem> items = new ArrayList<>();
        for (ChildItem childItem : childs) {
            if (childItem.type == FAQCategoryItemKind.ITEM) {
                items.add((FAQItemItem)childItem.data);
            }
        }
        return items;
    }

    @Override
    public List<FAQCategoryInfo> getSubCategories() {
        List<FAQCategoryInfo> subCategories = new ArrayList<>();
        for (ChildItem childItem : childs) {
            if (childItem.type == FAQCategoryItemKind.CATEGORY) {
                subCategories.add((FAQCategoryInfoItem)childItem.data);
            }
        }
        return subCategories;
    }

    public class ChildItem<T> {
        @SerializedName("type")
        private FAQCategoryItemKind type;
        @SerializedName("data")
        private T data;
    }

    public enum FAQCategoryItemKind {
        @SerializedName("item")
        ITEM,
        @SerializedName("category")
        CATEGORY
    }
}
