package ru.webim.android.demo.util;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class LongClickSupportMovementMethod extends LinkMovementMethod {

    private long lastClickTime = 0L;
    private int lastX = 0;
    private int lastY = 0;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int deltaX = Math.abs(x - lastX);
            int deltaY = Math.abs(y - lastY);

            lastX = x;
            lastY = y;

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            LongClickableSpan[] links = buffer.getSpans(off, off, LongClickableSpan.class);

            if (links.length != 0) {
                LongClickableSpan link = links[0];
                if (action == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - lastClickTime < 1000)
                        link.onClick(widget);
                    else if (deltaX < 10 && deltaY < 10)
                        link.onLongClick(widget);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                        buffer.getSpanStart(link),
                        buffer.getSpanEnd(link));
                    lastClickTime = System.currentTimeMillis();
                }
                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }


    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new LongClickSupportMovementMethod();

        return sInstance;
    }

    private static LongClickSupportMovementMethod sInstance;
}
