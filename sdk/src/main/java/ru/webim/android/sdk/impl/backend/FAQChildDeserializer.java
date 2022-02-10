package ru.webim.android.sdk.impl.backend;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ru.webim.android.sdk.impl.InternalUtils;
import ru.webim.android.sdk.impl.items.FAQCategoryInfoItem;
import ru.webim.android.sdk.impl.items.FAQCategoryItem;
import ru.webim.android.sdk.impl.items.FAQItemItem;

import java.lang.reflect.Type;

public class FAQChildDeserializer implements JsonDeserializer<FAQCategoryItem.ChildItem> {
    @Override
    public FAQCategoryItem.ChildItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonElement content = json.getAsJsonObject().get("type");

        FAQCategoryItem.FAQCategoryItemKind type = InternalUtils.fromJson(content.getAsString(),
                FAQCategoryItem.FAQCategoryItemKind.class);
        Type listType;
        if (type == null) {
            listType = new TypeToken<FAQCategoryItem.ChildItem<Object>>() {}.getType();
        } else {
            switch (type) {
                case CATEGORY:
                    listType = new TypeToken<FAQCategoryItem.ChildItem<FAQCategoryInfoItem>>() {}.getType();
                    break;
                case ITEM:
                    listType = new TypeToken<FAQCategoryItem.ChildItem<FAQItemItem>>() {}.getType();
                    break;
                default:
                    listType = new TypeToken<FAQCategoryItem.ChildItem<Object>>() {}.getType();
                    break;
            }
        }
        return InternalUtils.fromJson(json, listType);
    }
}
