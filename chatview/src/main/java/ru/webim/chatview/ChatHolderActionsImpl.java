package ru.webim.chatview;

import static ru.webim.android.sdk.Message.Type.FILE_FROM_OPERATOR;
import static ru.webim.android.sdk.Message.Type.FILE_FROM_VISITOR;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Objects;

import ru.webim.android.sdk.Message;
import ru.webim.android.sdk.MessageStream;
import ru.webim.chatview.holders.MessageHolder;
import ru.webim.chatview.ui.ChatEditBar;
import ru.webim.chatview.ui.ChatList;
import ru.webim.chatview.ui.ChatView;
import ru.webim.chatview.utils.AnchorMenuDialog;
import ru.webim.chatview.utils.ContextMenuDialog;
import ru.webim.chatview.utils.ViewUtils;

public class ChatHolderActionsImpl implements MessageHolder.ChatHolderActions {
    private final MessageStream stream;
    private final Context context;
    private final ChatEditBar editBar;
    private final ChatList chatList;
    private Dialog contextDialog;
    private String contextMessageId;

    public ChatHolderActionsImpl(ChatView chatView) {
        this.stream = chatView.getSession().getStream();
        this.context = chatView.getChatList().getContext();
        this.editBar = chatView.getChatEditBar();
        this.chatList = chatView.getChatList();
    }

    @Override
    public void onLinkClicked(String url, Message message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    public void onMessageUpdated(Message message, int adapterPosition) {
        String messageId = message.getServerSideId();
        if (contextDialog != null &&
            contextDialog.isShowing() &&
            contextDialog instanceof ContextMenuDialog &&
            messageId != null &&
            messageId.equals(contextMessageId)) {

            Message.Type type = message.getType();
            ContextMenuDialog contextMenuDialog = (ContextMenuDialog) contextDialog;
            contextMenuDialog.hideItems();
            boolean isFile = message.getType() == FILE_FROM_VISITOR || message.getType() == FILE_FROM_OPERATOR;
            contextMenuDialog.showItem(R.id.relLayoutReply, message.canBeReplied());
            contextMenuDialog.showItem(R.id.relLayoutCopy, !isFile);

            switch (type) {
                case OPERATOR:
                    contextMenuDialog.showItem(R.id.relLayoutEdit, false);
                    contextMenuDialog.showItem(R.id.relLayoutDelete, false);
                    contextMenuDialog.showItem(R.id.relLayoutDownload, false);
                    break;
                case FILE_FROM_OPERATOR:
                    contextMenuDialog.showItem(R.id.relLayoutEdit, false);
                    contextMenuDialog.showItem(R.id.relLayoutDelete, false);
                    contextMenuDialog.showItem(R.id.relLayoutDownload, true);
                    break;
                case VISITOR:
                    contextMenuDialog.showItem(R.id.relLayoutEdit, message.canBeEdited());
                    contextMenuDialog.showItem(R.id.relLayoutDelete, message.canBeEdited());
                    contextMenuDialog.showItem(R.id.relLayoutDownload, false);
                    break;
                case FILE_FROM_VISITOR:
                    contextMenuDialog.showItem(R.id.relLayoutEdit, false);
                    contextMenuDialog.showItem(R.id.relLayoutDelete, message.canBeEdited());
                    contextMenuDialog.showItem(R.id.relLayoutDownload, false);
                    break;
            }
        }
    }

    @Override
    public void onContextDialog(View visible, int adapterPosition, Message message) {
        chatList.getListController().scrollToPosition(adapterPosition);

        ContextMenuDialog dynamicDialog = new ContextMenuDialog(context);
        AnchorMenuDialog.OnMenuItemClickListener listener = itemId -> {
            if (itemId == R.id.relLayoutReply) {
                editBar.replyState(message);
            } else if (itemId == R.id.relLayoutEdit) {
                editBar.editState(message);
            } else if (itemId == R.id.relLayoutDelete) {
                stream.deleteMessage(message, null);
                editBar.sendingState();
            } else if (itemId == R.id.relLayoutCopy) {
                ClipData clip = ClipData.newPlainText("message", message.getText());
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    showToast(context.getResources().getString(R.string.copied_message), Toast.LENGTH_LONG);
                } else {
                    showToast(context.getResources().getString(R.string.copy_failed), Toast.LENGTH_LONG);
                }
            } else if (itemId == R.id.relLayoutDownload) {
                Message.Attachment attachment = message.getAttachment();
                if (attachment != null) {
                    Message.FileInfo fileInfo = attachment.getFileInfo();
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String fileName = resolveFileName(message.getServerSideId(), fileInfo);
                    Uri uri = Uri.withAppendedPath(Uri.fromFile(file), fileName);
                    onDownloadFile(fileInfo, uri, message);
                }
            }
            dynamicDialog.dismiss();
        };
        dynamicDialog.setMenu(R.layout.dialog_message_menu, R.id.context_menu_list);
        dynamicDialog.setOnMenuItemClickListener(listener);
        dynamicDialog.show(visible);

        contextDialog = dynamicDialog;
        contextMessageId = message.getServerSideId();
        onMessageUpdated(message, adapterPosition);
    }

    @NonNull
    private String resolveFileName(String messageId, Message.FileInfo fileInfo) {
        String fileName = fileInfo.getFileName();
        byte maxFilenameSize = Byte.MAX_VALUE;
        if (fileName.length() > maxFilenameSize) {
            String extension = fileInfo.getContentType() != null ? "." + fileInfo.getContentType() : "";
            fileName = messageId + extension;
        }
        return fileName;
    }

    @Override
    public void onBotButtonClicked(String buttonId, Message message) {
        stream.sendKeyboardRequest(Objects.requireNonNull(message.getServerSideId()), buttonId, null);
    }

    @Override
    public void onCacheFile(Message.FileInfo fileInfo, Uri cacheUri, Message message) {
        chatList.saveFile(message, cacheUri);
    }

    @Override
    public void onDownloadFile(Message.FileInfo fileInfo, Uri uri, Message message) {
        String filename = fileInfo.getFileName();
        showToast(context.getString(R.string.saving_file, filename), Toast.LENGTH_SHORT);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(fileInfo.getUrl()));
            downloadRequest.setTitle(filename);
            downloadRequest.allowScanningByMediaScanner();
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            downloadRequest.setDestinationUri(uri);
            manager.enqueue(downloadRequest);
        } else {
            showToast(context.getString(R.string.saving_failed), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onOpenFile(File file, Message.FileInfo fileInfo) {
        String packageName = context.getApplicationContext().getPackageName();
        Uri uri = FileProvider.getUriForFile(context, packageName + ".provider", file);
        String mime = fileInfo.getContentType();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, context.getString(R.string.share_file_title));

        try {
            context.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.cannot_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOpenImage(String url, Message message) {
        Activity requiredActivity = ViewUtils.getRequiredActivity(context);
        if (!(requiredActivity instanceof AppCompatActivity)) {
            Log.e(ChatHolderActionsImpl.class.getSimpleName(), "Host activity must be inherited from " + AppCompatActivity.class.getSimpleName() + " for opening image detail screen");
            return;
        }
        AppCompatActivity activity = (AppCompatActivity) requiredActivity;
        ImageDetailFragment fragment = ImageDetailFragment.withUri(url);
        fragment.show(activity.getSupportFragmentManager(), ImageDetailFragment.class.getSimpleName());
    }

    @Override
    public void onQuoteClicked(Message.Quote quote, int position, Message message) {
        chatList.getListController().scrollToMessage(quote.getMessageId());
    }

    private void showToast(String messageToast, int lengthToast) {
        Toast.makeText(context, messageToast, lengthToast).show();
    }
}
