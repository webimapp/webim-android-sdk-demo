package ru.webim.android.sdk.impl.items;

public class RatingItem {
    private String operatorId;
    private int rating;

    public RatingItem() {
        // Need for Gson No-args fix
    }

    public String getOperatorId() {
        return operatorId;
    }

    public int getRating() {
        return rating;
    }
}
