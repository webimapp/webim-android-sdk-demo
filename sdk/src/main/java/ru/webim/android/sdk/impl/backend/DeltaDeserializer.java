package ru.webim.android.sdk.impl.backend;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.webim.android.sdk.impl.InternalUtils;
import ru.webim.android.sdk.impl.items.ChatItem;
import ru.webim.android.sdk.impl.items.DepartmentItem;
import ru.webim.android.sdk.impl.items.HistoryRevisionItem;
import ru.webim.android.sdk.impl.items.MessageItem;
import ru.webim.android.sdk.impl.items.OperatorItem;
import ru.webim.android.sdk.impl.items.RatingItem;
import ru.webim.android.sdk.impl.items.SurveyItem;
import ru.webim.android.sdk.impl.items.UnreadByVisitorMessagesItem;
import ru.webim.android.sdk.impl.items.delta.DeltaItem;

import java.lang.reflect.Type;
import java.util.List;

class DeltaDeserializer implements JsonDeserializer<DeltaItem> {
    @Override
    public DeltaItem deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException {
        JsonElement content = je.getAsJsonObject().get("objectType");

        DeltaItem.Type deltaType
                = InternalUtils.fromJson(content.getAsString(), DeltaItem.Type.class);
        Type listType;
        if (deltaType == null) {
            listType = new TypeToken<DeltaItem<Object>>() {}.getType();
        } else {
            switch (deltaType) {
                case CHAT:
                    listType = new TypeToken<DeltaItem<ChatItem>>() {}.getType();
                    break;
                case CHAT_MESSAGE:
                    listType = new TypeToken<DeltaItem<MessageItem>>() {}.getType();
                    break;
                case CHAT_OPERATOR:
                    listType = new TypeToken<DeltaItem<OperatorItem>>() {}.getType();
                    break;
                case CHAT_OPERATOR_TYPING:
                case CHAT_READ_BY_VISITOR:
                    listType = new TypeToken<DeltaItem<Boolean>>() {}.getType();
                    break;
                case CHAT_STATE:
                    listType = new TypeToken<DeltaItem<String>>() {}.getType();
                    break;
                case DEPARTMENT_LIST:
                    listType = new TypeToken<DeltaItem<List<DepartmentItem>>>() {}.getType();
                    break;
                case HISTORY_REVISION:
                    listType = new TypeToken<DeltaItem<HistoryRevisionItem>>() {}.getType();
                    break;
                case OPERATOR_RATE:
                    listType = new TypeToken<DeltaItem<RatingItem>>() {}.getType();
                    break;
                case UNREAD_BY_VISITOR:
                    listType = new TypeToken<DeltaItem<UnreadByVisitorMessagesItem>>() {}.getType();
                    break;
                case VISIT_SESSION_STATE:
                    listType = new TypeToken<DeltaItem<String>>() {}.getType();
                    break;
                case SURVEY:
                    listType = new TypeToken<DeltaItem<SurveyItem>>() {}.getType();
                    break;
                default:
                    listType = new TypeToken<DeltaItem<Object>>() {}.getType();
                    break;
            }
        }
        return InternalUtils.fromJson(je, listType);
    }
}
