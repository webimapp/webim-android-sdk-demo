package ru.webim.chatview.holders;

import static android.content.Context.WINDOW_SERVICE;
import static ru.webim.android.sdk.Message.Type.FILE_FROM_VISITOR;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.Locale;

import ru.webim.android.sdk.Message;
import ru.webim.chatview.R;

public abstract class FileMessageHolder extends MessageHolder {
    ImageView thumbView;
    ImageView quoteView;
    ImageView fileImage;
    ConstraintLayout layoutQuotedView;
    ConstraintLayout layoutFileImage;
    TextView senderNameForImage;
    TextView fileName;
    TextView fileSize;
    TextView fileError;
    TextView textEdited;
    RelativeLayout layoutAttachedFile;
    ProgressBar progressFileUpload;
    CardView cardView;

    int bubbleColor;
    int selectedColor;
    int messageBackground;

    protected View.OnClickListener openContextMenu = view -> openContextDialog();

    private static class LongPressSupportTouchListener implements View.OnTouchListener {
        private final GestureDetectorCompat gestureDetector;
        private final Runnable action;
        private final GestureDetector.OnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                action.run();
            }
        };

        public LongPressSupportTouchListener(Context context, Runnable action) {
            this.gestureDetector = new GestureDetectorCompat(context, listener);
            this.action = action;
        }

        @Override
        public boolean onTouch(android.view.View view, MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            return view.onTouchEvent(event);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public FileMessageHolder(View itemView, ChatHolderActions actionsListener) {
        super(itemView, actionsListener);

        thumbView = itemView.findViewById(R.id.attached_image);
        quoteView = itemView.findViewById(R.id.quoted_image);
        layoutQuotedView = itemView.findViewById(R.id.const_quoted_image);
        senderNameForImage = itemView.findViewById(R.id.sender_name_for_image);
        layoutAttachedFile = itemView.findViewById(R.id.attached_file);
        fileImage = itemView.findViewById(R.id.file_image);
        layoutFileImage = itemView.findViewById(R.id.file_image_const);
        fileName = itemView.findViewById(R.id.file_name);
        fileSize = itemView.findViewById(R.id.file_size);
        fileError = itemView.findViewById(R.id.error_text);
        progressFileUpload = itemView.findViewById(R.id.progress_file_upload);
        cardView = itemView.findViewById(R.id.card_view);
        textEdited = itemView.findViewById(R.id.text_edited);

        layoutAttachedFile.setOnClickListener(openContextMenu);
        itemView.setOnClickListener(openContextMenu);

        itemView.setOnTouchListener(new LongPressSupportTouchListener(context, this::openContextDialog));
        layoutAttachedFile.setOnTouchListener(new LongPressSupportTouchListener(context, this::openContextDialog));
        thumbView.setOnTouchListener(new LongPressSupportTouchListener(context, this::openContextDialog));
        messageText.setOnTouchListener(new LongPressSupportTouchListener(context, this::openContextDialog));
    }

    @Override
    public void bind(Message message, boolean showDate) {
        super.bind(message, showDate);

        thumbView.setVisibility(View.GONE);
        layoutAttachedFile.setVisibility(View.GONE);
        layoutQuotedView.setVisibility(View.GONE);

        messageText.setOnClickListener(v -> openContextDialog());
        Message.Attachment attachment = message.getAttachment();
        if (attachment == null) {
            if (thumbView != null && message.getType().equals(FILE_FROM_VISITOR)) {
                thumbView.setVisibility(View.GONE);
                messageBody.setVisibility(View.VISIBLE);
                Resources resources = context.getResources();
                String textMessage = resources.getString(R.string.sending_file) + message.getText();
                messageText.setText(textMessage);
                messageText.setVisibility(View.VISIBLE);
            }
        } else {
            messageBody.setVisibility(View.GONE);
            Message.ImageInfo imageInfo = attachment.getFileInfo().getImageInfo();
            if (imageInfo == null || mustShowAttachmentView(imageInfo)) {
                showAttachmentView(attachment);
            } else {
                setViewSize(thumbView, getThumbSize(imageInfo));
                addImageInView(attachment.getFileInfo(), thumbView, messageText);
            }
        }

        Message.Quote messageQuote = message.getQuote();
        if (messageQuote != null &&
            messageQuote.getMessageAttachment() != null &&
            messageQuote.getMessageAttachment().getImageInfo() != null) {
            Message.FileInfo fileInfo = messageQuote.getMessageAttachment();
            addImageInView(fileInfo, quoteView, quoteText);
            layoutQuotedView.setVisibility(View.VISIBLE);
        }

        boolean notSending = message.getSendStatus() != Message.SendStatus.SENDING;
        textEdited.setVisibility(message.isEdited() && notSending ? View.VISIBLE : View.INVISIBLE);
    }

    private boolean mustShowAttachmentView(Message.ImageInfo imageInfo) {
        int maxSide = Math.max(imageInfo.getWidth(), imageInfo.getHeight());
        int minSide = Math.min(imageInfo.getWidth(), imageInfo.getHeight());
        int imageSideRatio = maxSide / minSide;
        int MAX_ALLOWED_ASPECT_RATIO = 10;
        return imageSideRatio > MAX_ALLOWED_ASPECT_RATIO;
    }

    private void showAttachmentView(final Message.Attachment attachment) {
        layoutAttachedFile.setVisibility(View.VISIBLE);
        Message.FileInfo fileInfo = attachment.getFileInfo();
        String filename = fileInfo.getFileName();
        this.fileName.setText(filename);
        fileSize.setVisibility(View.GONE);

        Resources resources = context.getResources();
        switch (attachment.getState()) {
            case READY:
                fileImage.setVisibility(View.VISIBLE);
                if (fileInfo.getImageInfo() != null) {
                    fileImage.setImageDrawable(resources.getDrawable(R.drawable.ic_image_attachment));
                    layoutAttachedFile.setOnClickListener(view -> holderActions.onOpenImage(fileInfo.getUrl(), message));
                } else {
                    File cacheDir = createFilesCacheDir();
                    String downloadFilename = getDownloadFilename(filename, message.getServerSideId());
                    File file = new File(cacheDir, downloadFilename);
                    if (file.exists()) {
                        fileImage.setImageDrawable(resources.getDrawable(R.drawable.ic_attachment));
                        fileImage.setOnClickListener(view -> holderActions.onOpenFile(file, fileInfo));
                    } else {
                        fileImage.setImageDrawable(resources.getDrawable(R.drawable.ic_download_icon));
                        Uri uri = resolveDownloadUri(downloadFilename, context);
                        fileImage.setOnClickListener(view -> holderActions.onCacheFile(fileInfo, uri, message));
                    }
                }
                progressFileUpload.setVisibility(View.GONE);
                fileSize.setVisibility(View.VISIBLE);
                String size = humanReadableByteCountBin(fileInfo.getSize());
                fileSize.setText(size);
                fileError.setVisibility(View.GONE);
                break;
            case UPLOAD:
                if (progressFileUpload.getVisibility() == View.GONE) {
                    fileImage.setVisibility(View.INVISIBLE);
                    progressFileUpload.setVisibility(View.VISIBLE);
                }
                fileError.setVisibility(View.VISIBLE);
                fileError.setText(resources.getString(R.string.file_transfer_by_operator));
                break;
            case ERROR:
                fileImage.setImageDrawable(resources.getDrawable(R.drawable.ic_error_download_file));
                progressFileUpload.setVisibility(View.GONE);
                fileError.setVisibility(View.VISIBLE);
                fileError.setText(attachment.getErrorMessage());
                fileImage.setVisibility(View.VISIBLE);
                break;
        }
    }

    @NonNull
    protected File createFilesCacheDir() {
        String directory = "webim";
        File cacheDir = new File(context.getExternalCacheDir() + "/" + directory);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private Uri resolveDownloadUri(String filename, Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        return Uri.withAppendedPath(Uri.fromFile(externalCacheDir), filename);
    }

    private String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
            : b <= 0xfffccccccccccccL >> 40 ? String.format(Locale.getDefault(), "%.0f kB", bytes / 0x1p10)
            : b <= 0xfffccccccccccccL >> 30 ? String.format(Locale.getDefault(), "%.0f MB", bytes / 0x1p20)
            : b <= 0xfffccccccccccccL >> 20 ? String.format(Locale.getDefault(), "%.0f GB", bytes / 0x1p30)
            : b <= 0xfffccccccccccccL >> 10 ? String.format(Locale.getDefault(), "%.0f TB", bytes / 0x1p40)
            : b <= 0xfffccccccccccccL ? String.format(Locale.getDefault(), "%.0f PiB", (bytes >> 10) / 0x1p40)
            : String.format(Locale.getDefault(), "%.0f EiB", (bytes >> 20) / 0x1p40);
    }

    private void addImageInView(
        Message.FileInfo fileInfo,
        ImageView imageView,
        TextView textView) {

        String imageUrl = fileInfo.getImageInfo().getThumbUrl();
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView);
        imageView.setOnClickListener(view -> holderActions.onOpenImage(fileInfo.getUrl(), message));
        textView.setText(context.getResources().getString(R.string.reply_message_with_image));
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
    }


    private Size getThumbSize(Message.ImageInfo imageInfo) {
        Size size;
        int imageWidth = imageInfo.getWidth();
        int imageHeight = imageInfo.getHeight();
        boolean isPortraitOrientation = imageHeight > imageWidth;
        if (isPortraitOrientation) {
            ScaledSize scaledSize = scalingThumbSize(
                imageWidth,
                imageHeight,
                isPortraitOrientation);
            size = new Size(scaledSize.getShotSide(), scaledSize.getLongSide());
        } else {
            ScaledSize scaledSize = scalingThumbSize(
                imageHeight,
                imageWidth,
                isPortraitOrientation);
            size = new Size(scaledSize.getLongSide(), scaledSize.getShotSide());
        }
        return size;
    }

    private ScaledSize scalingThumbSize(double shotSide, double longSide, boolean portraitOrientationImage) {
        double maxRatioForView = portraitOrientationImage ? 1 : 0.6;
        double minRatioForView = 0.17;
        int orientation = context.getResources().getConfiguration().orientation;
        DisplayMetrics displayMetrics = new DisplayMetrics();

        WindowManager manager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        double shorterScreenSide = orientation == Configuration.ORIENTATION_PORTRAIT
            ? displayMetrics.widthPixels
            : displayMetrics.heightPixels;
        double maxSize = shorterScreenSide * maxRatioForView;
        double minSize = shorterScreenSide * minRatioForView;
        double newShotSide = maxSize / longSide * shotSide;
        if (newShotSide < minSize) {
            shotSide = minSize;
        } else {
            shotSide = Math.min(newShotSide, maxSize);
        }
        longSide = maxSize;
        if (portraitOrientationImage) {
            double maxRatioForPortraitView = 0.6;
            double maxWidth = shorterScreenSide * maxRatioForPortraitView;
            if (shotSide > maxWidth) {
                longSide = maxWidth / shotSide * maxSize;
                shotSide = maxWidth;
            }
        }
        return new ScaledSize((int) shotSide, (int) longSide);
    }

    private void setViewSize(ImageView view, Size size) {
        if (view.getParent() instanceof FrameLayout) {
            view.setLayoutParams(new FrameLayout.LayoutParams(size.getWidth(), size.getHeight(), Gravity.END));
        }
        else {
            view.setLayoutParams(new LinearLayout.LayoutParams(size.getWidth(), size.getHeight(), Gravity.END));
        }
    }

    private Activity getRequiredActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    private class Size {
        private final int width;
        private final int height;

        private Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        int getWidth() {
            return width;
        }

        int getHeight() {
            return height;
        }
    }

    private class ScaledSize {
        private final int shotSide;
        private final int longSide;

        private ScaledSize(int shotSide, int longSide) {
            this.shotSide = shotSide;
            this.longSide = longSide;
        }

        int getShotSide() {
            return shotSide;
        }

        int getLongSide() {
            return longSide;
        }
    }

    void openContextDialog() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        int duration = 50;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }

        int adapterPosition = getAdapterPosition();
        holderActions.onContextDialog(getVisibleView(), adapterPosition, message);
    }

    private View getVisibleView() {
        switch (message.getType()) {
            case FILE_FROM_VISITOR:
            case FILE_FROM_OPERATOR:
                Message.Attachment attachment = message.getAttachment();
                if (attachment != null && attachment.getFileInfo().getImageInfo() != null) {
                    return cardView;
                } else {
                    return layoutAttachedFile;
                }
            default:
                return messageBody;
        }
    }

    /**
     * Note: Max filename size is 256 bytes
     */
    @NonNull
    public static String getDownloadFilename(String filename, String messageId) {
        int extensionIndex = filename.lastIndexOf('.');
        String extension = "";
        if (extensionIndex != -1) {
            extension = filename.substring(extensionIndex);
        }
        return messageId + extension;
    }
}
