package ru.webim.android.demo.client.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.cardview.widget.CardView;

import ru.webim.android.demo.client.R;

public class DynamicContextDialog extends Dialog implements DialogInterface.OnDismissListener {
    private View visibleView;
    private View menuView;
    private FrameLayout containerView;
    private int statusBarHeight;
    private int leftNavigationBarHeight;
    private int windowWidth;
    private int windowHeight;
    private float dimAmount;
    private final int visibleViewMargin;
    private Matrix matrix = new Matrix();
    private Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Activity parentActivity;
    private ConfigurationChangedListener configurationChangedListener;

    DynamicContextDialog(Context context, Activity parentActivity) {
        super(context, R.style.DynamicContextMenuTheme);

        this.parentActivity = parentActivity;

        containerView = new FrameLayout(getContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        visibleViewMargin = (int) context.getResources().getDimension(R.dimen.visible_area_margin);

        containerView.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (isShowing()) {
                if (configurationChangedListener != null) {
                    configurationChangedListener.configurationChanged();
                }
                visibleView.post(() -> {
                    calculateScreenSize();
                    drawVisibleArea();
                });
            }
        });
        setOnDismissListener(this);
        setContentView(containerView);
    }

    private void calculateScreenSize() {
        View usableView = parentActivity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        windowWidth = usableView.getWidth();
        windowHeight = usableView.getHeight();
        statusBarHeight = getScreenHeight(parentActivity.getBaseContext()) - windowHeight;

        Rect visibleFrame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);

        leftNavigationBarHeight = visibleFrame.left;
    }

    void setVisibleView(View visibleView) {
        this.visibleView = visibleView;
    }

    View setContextMenuRes(int contextMenuRes) {
        menuView = LayoutInflater.from(getContext()).inflate(contextMenuRes, containerView, false);
        menuView.setVisibility(View.INVISIBLE);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        containerView.removeAllViews();
        containerView.addView(menuView, layoutParams);
        return menuView;
    }

    void setDimAmount(float dimAmount) {
        this.dimAmount = dimAmount;
    }

    private void drawVisibleArea() {
        int[] positionArray = new int[2];
        visibleView.getLocationOnScreen(positionArray);
        setContextMenuGravity(positionArray);
        Bitmap bitmap = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.argb((int) (255 * dimAmount), 0, 0, 0));
        Bitmap visibleViewBitmap = bitmapFromView(visibleView);
        if (visibleView instanceof CardView) {
            CardView cardView = (CardView) visibleView;
            BitmapShader bitmapShader = new BitmapShader(visibleViewBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            matrix.reset();
            matrix.setTranslate(positionArray[0] - leftNavigationBarHeight, positionArray[1] - statusBarHeight);
            bitmapShader.setLocalMatrix(matrix);
            bitmapPaint.setShader(bitmapShader);
            canvas.drawRoundRect(
                new RectF(positionArray[0] - leftNavigationBarHeight,
                        positionArray[1] - statusBarHeight,
                        positionArray[0] - leftNavigationBarHeight + visibleView.getMeasuredWidth(),
                        positionArray[1] + visibleView.getMeasuredHeight() - statusBarHeight),
                cardView.getRadius(),
                cardView.getRadius(),
                bitmapPaint);
        } else {
            canvas.drawBitmap(
                visibleViewBitmap,
                new Rect(0, 0, visibleView.getMeasuredWidth(), visibleView.getMeasuredHeight()),
                new Rect(positionArray[0] - leftNavigationBarHeight,
                        positionArray[1] - statusBarHeight,
                        positionArray[0] - leftNavigationBarHeight + visibleView.getMeasuredWidth(),
                        positionArray[1] + visibleView.getMeasuredHeight() - statusBarHeight),
                null);
        }

        containerView.setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
        if (menuView != null && menuView.getVisibility() == View.INVISIBLE) {
            menuView.setVisibility(View.VISIBLE);
        }
    }

    private void setContextMenuGravity(int[] viewPosition) {
        if (menuView == null) {
            return;
        }

        int bottomPositionVisibleView = viewPosition[1] + visibleView.getHeight() - statusBarHeight;
        int topPositionVisibleView = viewPosition[1] - statusBarHeight;
        int leftPositionVisibleView = viewPosition[0] - leftNavigationBarHeight;
        int rightPositionVisibleView = viewPosition[0] + visibleView.getWidth() - leftNavigationBarHeight;
        int menuViewHeight = menuView.getHeight();
        int menuViewWidth = menuView.getWidth();

        if (bottomPositionVisibleView + visibleViewMargin + menuViewHeight <= windowHeight) {
            // align below of view
            menuView.setY(bottomPositionVisibleView + visibleViewMargin);
        } else if (topPositionVisibleView - visibleViewMargin - menuViewHeight >= 0) {
            // align above of view
            menuView.setY(topPositionVisibleView - visibleViewMargin - menuViewHeight);
        } else {
            // align right / left / center of view
            menuView.setY(windowHeight - menuViewHeight - visibleViewMargin);
            if (windowWidth - rightPositionVisibleView + visibleViewMargin >= menuViewWidth) {
                menuView.setX(rightPositionVisibleView + visibleViewMargin);
            } else if (leftPositionVisibleView + visibleViewMargin >= menuViewWidth) {
                menuView.setX(leftPositionVisibleView - visibleViewMargin - menuViewWidth);
            } else {
                menuView.setX(windowWidth / 2f - menuViewWidth / 2f);
            }
            return;
        }

        if (menuViewWidth <= visibleView.getWidth()) {
            // align center horizontally of view
            menuView.setX(leftPositionVisibleView + visibleView.getWidth() / 2f - menuViewWidth / 2f);
        } else if (leftPositionVisibleView + menuViewWidth > windowWidth) {
            // align to the right of view
            menuView.setX(rightPositionVisibleView - menuViewWidth);
        } else {
            // align to the left of view
            menuView.setX(leftPositionVisibleView);
        }
    }

    private int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point screenSize = new Point();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getSize(screenSize);
        }
        return screenSize.y;
    }

    private Bitmap bitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (menuView != null) {
            menuView.setVisibility(View.INVISIBLE);
        }
        containerView.setBackground(new ColorDrawable(Color.TRANSPARENT));
    }

    void setConfigurationChangedListener(ConfigurationChangedListener configurationChangedListener) {
        this.configurationChangedListener = configurationChangedListener;
    }

    void setOnShadowAreaClickListener(View.OnClickListener clickListener) {
        containerView.setOnClickListener(clickListener);
    }

    public interface ConfigurationChangedListener {
        void configurationChanged();
    }
}
