package ru.webim.android.demo.client;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.webim.android.demo.client.R;
import ru.webim.android.sdk.Message;

import java.lang.ref.WeakReference;
import java.util.List;

class KeyboardAdapter {
    private final KeyboardButtonClickListener keyboardButtonClickListener;
    private final WeakReference<LinearLayout> linearLayout;
    private final WeakReference<Context> context;
    private Message.Keyboard.State keyboardState;
    private String selectedButtonId;

    private final int pendingTextColor;
    private final int canceledTextColor;
    private final int completedTextColor;

    private final int buttonTextSize;
    private final int buttonPadding;
    private final int buttonMarginTop;
    private final int buttonMarginBottom;
    private final int buttonMarginLeft;
    private final int buttonMarginRight;

    KeyboardAdapter(LinearLayout keyboardLayout, KeyboardButtonClickListener keyboardButtonClickListener) {
        this.linearLayout = new WeakReference<>(keyboardLayout);
        this.keyboardButtonClickListener = keyboardButtonClickListener;
        this.context = new WeakReference<>(keyboardLayout.getContext());

        pendingTextColor = ContextCompat.getColor(context.get(), R.color.colorPendingKeyboardButton);
        canceledTextColor = ContextCompat.getColor(context.get(), R.color.colorCanceledKeyboardButton);
        completedTextColor = ContextCompat.getColor(context.get(), R.color.colorCompletedKeyboardButton);

        buttonTextSize = (int) context.get().getResources().getDimension(R.dimen.button_text_size);
        buttonPadding = (int) context.get().getResources().getDimension(R.dimen.button_padding);
        buttonMarginTop = (int) context.get().getResources().getDimension(R.dimen.button_margin_top);
        buttonMarginBottom = (int) context.get().getResources().getDimension(R.dimen.button_margin_bottom);
        buttonMarginLeft = (int) context.get().getResources().getDimension(R.dimen.button_margin_left);
        buttonMarginRight = (int) context.get().getResources().getDimension(R.dimen.button_margin_right);
        int keyboardPadding = (int) context.get().getResources().getDimension(R.dimen.keyboard_padding);
        keyboardLayout.setPadding(keyboardPadding, keyboardPadding, keyboardPadding, keyboardPadding);
    }

    void showKeyboard(Message.Keyboard keyboard) {
        showKeyboardButtons(keyboard);
    }

    private void showKeyboardButtons(Message.Keyboard keyboard) {
        LinearLayout keyboardLayout = linearLayout.get();

        if (keyboardLayout == null) {
            return;
        }
        keyboardLayout.removeAllViews();

        if (keyboard == null || keyboard.getButtons() == null || keyboard.getState() == null) {
            return;
        }

        keyboardState = keyboard.getState();
        if (keyboard.getKeyboardResponse() != null) {
            selectedButtonId = keyboard.getKeyboardResponse().getButtonId();
        }
        List<List<Message.KeyboardButtons>> keyboardButtons = keyboard.getButtons();

        for (List<Message.KeyboardButtons> buttonsInRow : keyboardButtons) {
            LinearLayout buttonsRow = new LinearLayout(context.get());
            buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            keyboardLayout.addView(buttonsRow, layoutParams);
            for (final Message.KeyboardButtons button : buttonsInRow) {
                TextView textView = new TextView(context.get());
                textView.setText(button.getText());
                ViewCompat.setBackground(textView, getButtonBackgroundDrawable(button.getId()));
                textView.setTextColor(getButtonTextColor(button.getId()));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize);
                textView.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
                textView.setGravity(Gravity.CENTER);
                textView.setOnClickListener(view -> keyboardButtonClickListener.keyboardButtonClick(button.getId()));
                LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
                textViewLayoutParams.weight = 1;
                textViewLayoutParams.setMargins(buttonMarginLeft, buttonMarginTop, buttonMarginRight, buttonMarginBottom);
                buttonsRow.addView(textView, textViewLayoutParams);
            }
        }
    }

    private int getButtonTextColor(String buttonId) {
        switch (keyboardState) {
            case PENDING:
                return pendingTextColor;
            case CANCELLED:
                return canceledTextColor;
            case COMPLETED:
            default:
                if (selectedButtonId != null && selectedButtonId.equals(buttonId)) {
                    return completedTextColor;
                } else {
                    return canceledTextColor;
                }
        }
    }

    private Drawable getButtonBackgroundDrawable(String buttonId) {
        Drawable pressedButtonBackground = ContextCompat.getDrawable(context.get(), R.drawable.background_bot_button_pressed);
        Drawable unpressedButtonBackground = ContextCompat.getDrawable(context.get(), R.drawable.background_bot_button_unpressed);
        switch (keyboardState) {
            case PENDING:
            case CANCELLED:
                return unpressedButtonBackground;
            case COMPLETED:
            default:
                if (selectedButtonId != null && selectedButtonId.equals(buttonId)) {
                    return pressedButtonBackground;
                } else {
                    return unpressedButtonBackground;
                }
        }
    }

    public interface KeyboardButtonClickListener {
        void keyboardButtonClick(String buttonId);
    }
}
