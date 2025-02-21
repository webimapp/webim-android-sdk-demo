package ru.webim.chatview.holders;

import android.view.View;
import android.widget.LinearLayout;

import ru.webim.android.sdk.Message;
import ru.webim.chatview.KeyboardAdapter;
import ru.webim.chatview.R;

public class BotMessageHolder extends MessageHolder {
    LinearLayout keyboardView;
    KeyboardAdapter keyboardAdapter;

    public BotMessageHolder(View itemView, ChatHolderActions holderActions) {
        super(itemView, holderActions);

        keyboardView = itemView.findViewById(R.id.lay_bot_keyboard);
        keyboardAdapter = new KeyboardAdapter(keyboardView, buttonId -> holderActions.onBotButtonClicked(buttonId, message));
    }

    @Override
    public void bind(final Message message, boolean showDate) {
        super.bind(message, showDate);

        messageText.setVisibility(View.GONE);
        keyboardAdapter.showKeyboard(message.getKeyboard());
    }
}

