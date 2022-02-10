package ru.webim.android.sdk.impl.items;

import com.google.gson.annotations.SerializedName;
import ru.webim.android.sdk.FAQItem;

import java.util.List;

public class FAQItemItem implements FAQItem {
    @SerializedName("id")
    private String id;
    @SerializedName("categories")
    private List<String> categories;
    @SerializedName("title")
    private String title;
    @SerializedName("tags")
    private List<String> tags;
    @SerializedName("content")
    private String content;
    @SerializedName("likes")
    private int likes;
    @SerializedName("dislikes")
    private int dislikes;
    @SerializedName("userRate")
    private FAQUserRateKind userRate;

    public FAQItemItem(FAQItem faqItem, UserRate userRate) {
        UserRate previousUserRate = faqItem.getUserRate();
        this.id = faqItem.getId();
        this.categories = faqItem.getCategories();
        this.title = faqItem.getTitle();
        this.tags = faqItem.getTags();
        this.content = faqItem.getContent();
        this.likes = faqItem.getLikeCount()
                + (userRate == UserRate.LIKE ? 1 : 0)
                - (previousUserRate == UserRate.LIKE ? 1 : 0);
        this.dislikes = faqItem.getDislikeCount()
                + (userRate == UserRate.DISLIKE ? 1 : 0)
                - (previousUserRate == UserRate.DISLIKE ? 1 : 0);
        this.userRate = toFAQUesrRateKind(userRate);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public int getLikeCount() {
        return likes;
    }

    @Override
    public int getDislikeCount() {
        return dislikes;
    }

    @Override
    public UserRate getUserRate() {
        if (userRate == null) {
            return UserRate.NO_RATE;
        }
        switch (userRate) {
            case LIKE:
                return UserRate.LIKE;
            case DISLIKE:
                return UserRate.DISLIKE;
            default:
                return UserRate.NO_RATE;
        }
    }

    private enum FAQUserRateKind {
        @SerializedName("like")
        LIKE,
        @SerializedName("dislike")
        DISLIKE
    }

    private FAQUserRateKind toFAQUesrRateKind(UserRate userRate) {
        return userRate == UserRate.LIKE ? FAQUserRateKind.LIKE : FAQUserRateKind.DISLIKE;
    }
}
