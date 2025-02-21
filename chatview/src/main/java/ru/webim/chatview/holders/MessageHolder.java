package ru.webim.chatview.holders;

import static ru.webim.android.sdk.Message.Type.FILE_FROM_OPERATOR;
import static ru.webim.android.sdk.Message.Type.FILE_FROM_VISITOR;
import static ru.webim.android.sdk.Message.Type.KEYBOARD_RESPONSE;
import static ru.webim.android.sdk.Message.Type.OPERATOR;
import static ru.webim.android.sdk.Message.Type.VISITOR;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.webim.android.sdk.Message;
import ru.webim.chatview.LongClickSupportMovementMethod;
import ru.webim.chatview.R;
import ru.webim.chatview.utils.LongClickableSpan;

public class MessageHolder extends RecyclerView.ViewHolder {
    protected Context context;
    protected ChatHolderActions holderActions;
    protected Message message;
    protected TextView messageText;
    protected TextView messageDate;
    protected TextView messageTime;
    protected ImageView messageTick;
    protected ViewGroup quoteLayout;
    protected ViewGroup quoteBody;
    protected TextView quoteSenderName;
    protected TextView quoteText;
    protected ViewGroup messageBody;
    private final Pattern pattern = Pattern.compile("\\b(https://|http://)?([-a-zA-Z0-9а-яА-ЯёЁ_@#]+\\.)+[a-zA-Zа-яА-ЯёЁ]+(/[-a-zA-Z0-9а-яА-ЯёЁ_@%#+.!:]+)*(\\.[-a-zA-Z_]+)?(/?\\?[-a-zA-Z0-9а-яА-ЯёЁ_@%#=&+.!:]+)?/?");

    public MessageHolder(View itemView, ChatHolderActions holderActions) {
        super(itemView);

        this.holderActions = holderActions;
        this.context = itemView.getContext();

        messageText = itemView.findViewById(R.id.text_message_body);
        messageDate = itemView.findViewById(R.id.text_message_date);
        messageTime = itemView.findViewById(R.id.text_message_time);
        messageTick = itemView.findViewById(R.id.tick);
        quoteLayout = itemView.findViewById(R.id.quote_message);
        quoteBody = itemView.findViewById(R.id.quote_body);
        quoteSenderName = itemView.findViewById(R.id.quote_sender_name);
        quoteText = itemView.findViewById(R.id.quote_text);
        messageBody = itemView.findViewById(R.id.message_body);

        messageText.setMovementMethod(LongClickSupportMovementMethod.getInstance());
        messageText.setHighlightColor(Color.TRANSPARENT);
    }

    public void bind(final Message message, boolean showDate) {
        this.message = message;
        holderActions.onMessageUpdated(message, getAdapterPosition());

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (message.getType() == KEYBOARD_RESPONSE) {
            layoutParams.height = 0;
            return;
        }
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        messageBody.setVisibility(View.GONE);
        if (messageText != null &&
            message.getType() != FILE_FROM_OPERATOR &&
            message.getType() != FILE_FROM_VISITOR) {
            messageBody.setVisibility(View.VISIBLE);
            messageText.setText(handleHyperlinks(handleHyperlinksTemplate(message.getText())));
            messageText.setVisibility(View.VISIBLE);
        }
        if (messageDate != null) {
            if (showDate) {
                messageDate
                    .setText(DateFormat.getDateFormat(context)
                        .format(message.getTime()));
                messageDate.setVisibility(View.VISIBLE);
            } else {
                messageDate.setVisibility(View.GONE);
            }
        }
        if (messageTime != null) {
            messageTime
                .setText(DateFormat.getTimeFormat(context)
                    .format(message.getTime()));
            messageText.setVisibility(View.VISIBLE);
        }

        if (messageTick != null) {
            messageTick.setImageResource(message.isReadByOperator()
                ? R.drawable.ic_double_tick
                : R.drawable.ic_tick);
            messageTick.setVisibility(message.getSendStatus() == Message.SendStatus.SENT
                ? View.VISIBLE
                : View.INVISIBLE);
        }

        if (quoteLayout != null) {
            if (message.getQuote() != null) {
                quoteLayout.setVisibility(View.VISIBLE);
                quoteSenderName.setVisibility(View.VISIBLE);
                quoteText.setVisibility(View.VISIBLE);
                Resources resources = context.getResources();
                Message.Quote quote = message.getQuote();
                quoteLayout.setOnClickListener((v) -> holderActions.onQuoteClicked(message.getQuote(), getAdapterPosition(), message));
                String textQuoteSenderName = "";
                String textQuote = "";
                switch (quote.getState()) {
                    case PENDING:
                        textQuote = (message.getType() == OPERATOR)
                            ? resources.getString(R.string.quote_is_pending)
                            : quote.getMessageText();
                        textQuoteSenderName = (message.getType() == OPERATOR)
                            ? ""
                            : resources.getString(R.string.visitor_sender_name);
                        break;
                    case FILLED:
                        textQuote =
                            (quote.getMessageType() == FILE_FROM_OPERATOR
                                || quote.getMessageType() == FILE_FROM_VISITOR)
                                ? quote.getMessageAttachment().getFileName()
                                : quote.getMessageText();
                        textQuoteSenderName =
                            (quote.getMessageType() == VISITOR
                                || quote.getMessageType() == FILE_FROM_VISITOR)
                                ? resources.getString(R.string.visitor_sender_name)
                                : quote.getSenderName();
                        break;
                    case NOT_FOUND:
                        textQuote = resources.getString(R.string.quote_is_not_found);
                        quoteSenderName.setVisibility(View.GONE);
                        break;
                }
                quoteSenderName.setText(textQuoteSenderName);
                quoteText.setText(textQuote);
            } else {
                quoteLayout.setVisibility(View.GONE);
                quoteSenderName.setVisibility(View.GONE);
                quoteText.setVisibility(View.GONE);
            }
        }

        if (messageText != null) {
            messageText.requestLayout();
        }
        if (quoteText != null) {
            quoteText.requestLayout();
        }
    }

    private CharSequence handleHyperlinksTemplate(CharSequence originalString) {
        Pattern templatePattern = Pattern.compile("(\\[(\\S+)\\]\\((\\S+://\\S+)\\))");
        Matcher matcher = templatePattern.matcher(originalString);

        if (!matcher.find()) {
            return originalString;
        }
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(originalString);

        do {
            int start = matcher.start();
            int end = matcher.end();

            int startLink = matcher.start(1);
            int endLink = matcher.end(1);
            if ((startLink | endLink) == -1) {
                continue;
            }

            int startTemplate = matcher.start(2);
            int endTemplate = matcher.end(2);
            if ((startTemplate | endTemplate) == -1) {
                continue;
            }

            String urlString = originalString.toString().substring(startLink, endLink);
            String templateString = originalString.toString().substring(startTemplate, endTemplate);

            spannableBuilder.replace(start, end, makeHyperlinkClickable(templateString, urlString));
        } while (matcher.find());

        return spannableBuilder;
    }

    private CharSequence handleHyperlinks(CharSequence originalString) {
        Matcher matcher = pattern.matcher(originalString);

        if (!matcher.find()) {
            return originalString;
        }

        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(originalString);
        boolean matchesFirst = true;

        while (matchesFirst || matcher.find()) {
            int startLink = matcher.start();
            int endLink = matcher.end();
            if ((startLink | endLink) == -1) {
                continue;
            }
            String urlString = originalString.toString().substring(startLink, endLink);
            spannableBuilder.replace(startLink, endLink, makeHyperlinkClickable(urlString, urlString));
            matchesFirst = false;
        }
        return spannableBuilder;
    }

    private Spannable makeHyperlinkClickable(String hyperlinkText, final String url) {
        SpannableString spannableString = new SpannableString(hyperlinkText);
        LongClickableSpan clickableSpan = new LongClickableSpan() {
            @Override
            public void onLongClick(View view) {
                if (MessageHolder.this instanceof FileMessageHolder) {
                    ((FileMessageHolder) MessageHolder.this).openContextDialog();
                }
            }

            @Override
            public void onClick(@NonNull View widget) {
                String urlClicked = url;
                String httpsProtocol = "https://";
                if (!urlClicked.contains(httpsProtocol)) {
                    urlClicked = httpsProtocol + urlClicked;
                }
                holderActions.onLinkClicked(urlClicked, message);
            }
        };
        spannableString.setSpan(clickableSpan, 0, hyperlinkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public interface ChatHolderActions {

        void onLinkClicked(String url, Message message);

        void onMessageUpdated(Message message, int adapterPosition);

        void onContextDialog(View visible, int position, Message message);

        void onBotButtonClicked(String buttonId, Message message);

        void onCacheFile(Message.FileInfo fileInfo, Uri cacheUri, Message message);

        void onDownloadFile(Message.FileInfo fileInfo, Uri uri, Message message);

        void onOpenFile(File file, Message.FileInfo fileInfo);

        void onOpenImage(String url, Message message);

        void onQuoteClicked(Message.Quote quote, int position, Message message);
    }
}
