package ru.webim.chatview.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.util.TypedValue;

public class ViewUtils {
    public static int resolveAttr(int attr, Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static Activity getRequiredActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
