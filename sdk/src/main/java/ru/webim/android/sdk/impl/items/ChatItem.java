package ru.webim.android.sdk.impl.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChatItem {
    @SerializedName("category")
    private String category;
    @SerializedName("clientSideId")
    private String clientSideId;
    @SerializedName("creationTimestamp")
    private double creationTimestamp;
    @SerializedName("id")
    private String id;
    @SerializedName("messages")
    private List<MessageItem> messages;
    @SerializedName("modificationTimestamp")
    private double modificationTimestamp;
    @SerializedName("offline")
    private Boolean offline;
    @SerializedName("operator")
    private OperatorItem operator;
    @SerializedName("operatorIdToRate")
    private Map<String, RatingItem> operatorIdToRate;
    @SerializedName("operatorTyping")
    private Boolean operatorTyping;
    @SerializedName("readByVisitor")
    private Boolean readByVisitor;
    @SerializedName("state")
    private String state;
    @SerializedName("subcategory")
    private String subcategory;
    @SerializedName("subject")
    private String subject;
    @SerializedName("unreadByVisitorMsgCnt")
    private int unreadByVisitorMessageCount;
    @SerializedName("unreadByOperatorSinceTs")
    private double unreadByOperatorTimestamp;
    @SerializedName("unreadByVisitorSinceTs")
    private double unreadByVisitorTimestamp;
    @SerializedName("visitorTyping")
    private Boolean visitorTyping;

    public ChatItem() {
        creationTimestamp = System.currentTimeMillis() / 1000L;
        id = String.valueOf(((int) (-creationTimestamp)));
        messages = new ArrayList<>();
    }

    public ChatItem(String id) {
        creationTimestamp = System.currentTimeMillis() / 1000L;
        this.id = id;
        messages = new ArrayList<>();
    }

    public boolean isOperatorTyping() {
        return operatorTyping;
    }

    public void setOperatorTyping(boolean operatorTyping) {
        this.operatorTyping = operatorTyping;
    }

    public String getId() {
        return id;
    }

    public long getUnreadByVisitorTimestamp() {
        return (long) (unreadByVisitorTimestamp * 1000L);
    }

    public void setUnreadByVisitorTimestamp(double unreadByVisitorTimestamp) {
        this.unreadByVisitorTimestamp = unreadByVisitorTimestamp;
    }

    public long getUnreadByOperatorTimestamp() {
        return (long) (unreadByOperatorTimestamp * 1000L);
    }

    public void setUnreadByOperatorTimestamp(double unreadByOperatorTimestamp) {
        this.unreadByOperatorTimestamp = unreadByOperatorTimestamp;
    }

    public int getUnreadByVisitorMessageCount() {
        return unreadByVisitorMessageCount;
    }

    public void setUnreadByVisitorMessageCount(int unreadByVisitorMessageCount) {
        this.unreadByVisitorMessageCount = unreadByVisitorMessageCount;
    }

    public List<MessageItem> getMessages() {
        return messages;
    }

    public ItemChatState getState() {
        return ItemChatState.getType(state);
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setState(ItemChatState state) {
        this.state = state.getTypeValue();
    }

    public void addMessage(MessageItem message) {
        addMessage(null, message);
    }

    public void addMessage(Integer position, MessageItem message) {
        if (position == null) {
            messages.add(message);
        } else {
            messages.add(position, message);
        }
    }

    public OperatorItem getOperator() {
        return operator;
    }

    public void setOperator(OperatorItem operator) {
        this.operator = operator;
    }

    public Boolean isReadByVisitor() {
        return readByVisitor;
    }

    public void setReadByVisitor(Boolean readByVisitor) {
        this.readByVisitor = readByVisitor;
    }

    public String getClientSideId() {
        return clientSideId;
    }

    @NonNull
    public Map<String, RatingItem> getOperatorIdToRating() {
        if (operatorIdToRate == null) {
            operatorIdToRate = new HashMap<>();
        }
        return operatorIdToRate;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof ChatItem && this.getId() != null && this.getClientSideId() != null) {
            ChatItem chatItem = (ChatItem) object;
            return this.getId().equals(chatItem.getId())
                    && this.clientSideId.equals(chatItem.clientSideId);
        } else {
            return false;
        }
    }

    public enum ItemChatState {
        CHATTING("chatting"),
        CHATTING_WITH_ROBOT("chatting_with_robot"),
        CLOSED("closed"),
        CLOSED_BY_OPERATOR("closed_by_operator"),
        CLOSED_BY_VISITOR("closed_by_visitor"),
        DELETED("deleted"),
        INVITATION("invitation"),
        ROUTING("routing"),
        QUEUE("queue"),
        UNKNOWN("unknown");

        private String typeValue;

        ItemChatState(String type) {
            typeValue = type;
        }

        public boolean isClosed() {
            return this == CLOSED
                    || this == CLOSED_BY_OPERATOR
                    || this == CLOSED_BY_VISITOR
                    || this == DELETED
                    || this == UNKNOWN;
        }

        public boolean isOpen() {
            return !isClosed();
        }

        static public ItemChatState getType(String pType) {
            for (ItemChatState type : ItemChatState.values()) {
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
}
