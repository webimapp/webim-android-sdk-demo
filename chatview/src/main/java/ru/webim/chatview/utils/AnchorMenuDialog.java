package ru.webim.chatview.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class AnchorMenuDialog extends Dialog {
    public Map<Integer, View> itemToViewMap = new HashMap<>();

    protected OnMenuItemClickListener listener;
    protected FrameLayout containerView;
    protected View menuView;
    protected View anchorView;
    protected int indentTop;
    protected int indentLeft;
    protected int visibleWindowWidth;
    protected int visibleWindowHeight;
    private boolean firstChange = true;

    private ConfigurationChangedListener configurationChangedListener;
    private boolean cancelable = true;
    private int gravity = Gravity.NO_GRAVITY;
    private float disableOpacity = 1.0f;

    public AnchorMenuDialog(@NonNull Context context) {
        this(context, 0);
    }

    public AnchorMenuDialog(@NonNull Context context, int styleResource) {
        super(context, styleResource);

        containerView = new FrameLayout(context);
    }

    @Override
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public float getDisableOpacity() {
        return disableOpacity;
    }

    public void setDisableOpacity(float disableOpacity) {
        this.disableOpacity = disableOpacity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(containerView);

        Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) menuView.getLayoutParams();
        layoutParams.gravity = gravity;
        menuView.setLayoutParams(layoutParams);

        containerView.setOnClickListener((v) -> {
            if (cancelable) {
                dismiss();
            }
        });

        menuView.setVisibility(View.VISIBLE);
        invalidateMenu();
        containerView.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (isShowing()) {
                if (configurationChangedListener != null) {
                    configurationChangedListener.configurationChanged();
                }
                if (!firstChange) {
                    int delayInvalidate = 500;
                    containerView.postDelayed(this::invalidateMenu, delayInvalidate);
                }
                firstChange = false;
            }
        });
    }

    private void invalidateMenu() {
        containerView.post(() -> {
            calculateScreenSize();
            if (anchorView != null) {
                int[] anchorViewPosition = new int[2];
                anchorView.getLocationOnScreen(anchorViewPosition);
                setMenuGravity(anchorViewPosition);
                onMenuCalculated(anchorViewPosition);
            }
        });
    }

    protected void onMenuCalculated(int[] anchorViewPosition) {
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void show(View anchorView) {
        if (anchorView.getWidth() == 0 || anchorView.getHeight() == 0) return;

        this.anchorView = anchorView;
        show();
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.listener = listener;
    }

    public void enableItem(int itemId, boolean enabled) {
        View view = itemToViewMap.get(itemId);
        if (view != null) {
            view.setAlpha(enabled ? 1.0f : disableOpacity);
            view.setEnabled(enabled);
        }
    }

    public void enableItems() {
        for (Map.Entry<Integer, View> entry : itemToViewMap.entrySet()) {
            View view = entry.getValue();
            view.setAlpha(1.0f);
            entry.getValue().setEnabled(true);
        }
    }

    public void disableItems() {
        for (Map.Entry<Integer, View> entry : itemToViewMap.entrySet()) {
            View view = entry.getValue();
            view.setAlpha(disableOpacity);
            view.setEnabled(false);
        }
    }

    public void showItem(int itemId, boolean show) {
        View view = itemToViewMap.get(itemId);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void showItems() {
        for (Map.Entry<Integer, View> entry : itemToViewMap.entrySet()) {
            entry.getValue().setVisibility(View.VISIBLE);
        }
    }

    public void hideItems() {
        for (Map.Entry<Integer, View> entry : itemToViewMap.entrySet()) {
            entry.getValue().setVisibility(View.GONE);
        }
    }

    public boolean itemShown(int itemId) {
        View item = itemToViewMap.get(itemId);
        if (item != null) {
            return item.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    public boolean itemEnabled(int itemId) {
        View item = itemToViewMap.get(itemId);
        if (item != null) {
            return item.isEnabled();
        }
        return false;
    }

    private void calculateScreenSize() {
        Rect visibleFrame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);

        indentLeft = visibleFrame.left;
        indentTop = visibleFrame.top;

        visibleWindowWidth = visibleFrame.right - visibleFrame.left;
        visibleWindowHeight = visibleFrame.bottom - visibleFrame.top;
    }

    public View setMenu(int contextMenuRes) {
        View inflatedView = LayoutInflater.from(getContext()).inflate(contextMenuRes, containerView, false);
        return setMenu(inflatedView, 0);
    }

    public View setMenu(int contextMenuRes, int itemsContainerId) {
        View inflatedView = LayoutInflater.from(getContext()).inflate(contextMenuRes, containerView, false);
        return setMenu(inflatedView, itemsContainerId);
    }

    public View setMenu(View menuView) {
        return setMenu(menuView, 0);
    }

    public View setMenu(View menuView, int itemsContainerId) {
        this.menuView = menuView;

        menuView.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams menuParams = menuView.getLayoutParams();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(menuParams.width, menuParams.height);
        containerView.removeAllViews();
        containerView.addView(menuView, layoutParams);

        ViewGroup itemsContainer = (ViewGroup) menuView;
        if (itemsContainerId != 0) {
            itemsContainer = menuView.findViewById(itemsContainerId);
        }

        for (int i = 0; i < itemsContainer.getChildCount(); i++) {
            View child = itemsContainer.getChildAt(i);
            int childId = child.getId();
            itemToViewMap.put(childId, child);
            child.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatMenuItemClicked(childId);
                }
            });
        }

        return menuView;
    }

    private void setMenuGravity(int[] viewPosition) {
        if (menuView == null) {
            return;
        }

        int bottomPositionVisibleView = viewPosition[1] + anchorView.getHeight() - indentTop;
        int topPositionVisibleView = viewPosition[1] - indentTop;
        int leftPositionVisibleView = viewPosition[0] - indentLeft;
        int rightPositionVisibleView = viewPosition[0] + anchorView.getWidth() - indentLeft;
        int menuViewHeight = menuView.getHeight();
        int menuViewWidth = menuView.getWidth();

        float xCoord = 0;
        float yCoord = 0;

        if (topPositionVisibleView - menuViewHeight >= 0) {
            // align above of view
            yCoord = topPositionVisibleView - menuViewHeight;
        } else if (bottomPositionVisibleView + menuViewHeight <= visibleWindowHeight) {
            // align below of view
            yCoord = bottomPositionVisibleView;
        } else {
            // align right / left / center of view
            yCoord = visibleWindowHeight - menuViewHeight;
            if (visibleWindowWidth - rightPositionVisibleView >= menuViewWidth) {
                xCoord = rightPositionVisibleView;
            } else if (leftPositionVisibleView >= menuViewWidth) {
                xCoord = leftPositionVisibleView - menuViewWidth;
            } else {
                xCoord = visibleWindowWidth / 2f - menuViewWidth / 2f;
            }
            menuView.setX(xCoord);
            menuView.setY(yCoord);
            return;
        }

        if (menuViewWidth <= anchorView.getWidth()) {
            // align center horizontally of view
            xCoord = leftPositionVisibleView + anchorView.getWidth() / 2f - menuViewWidth / 2f;
        } else if (leftPositionVisibleView + menuViewWidth > visibleWindowWidth) {
            // align to the right of view
            xCoord = rightPositionVisibleView - menuViewWidth;
        } else {
            // align to the left of view
            xCoord = leftPositionVisibleView;
        }

        int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        if (horizontalGravity == Gravity.NO_GRAVITY) {
            menuView.setX(xCoord);
        }
        if (verticalGravity == Gravity.NO_GRAVITY) {
            menuView.setY(yCoord);
        }
    }

    void setConfigurationChangedListener(ConfigurationChangedListener configurationChangedListener) {
        this.configurationChangedListener = configurationChangedListener;
    }

    public interface ConfigurationChangedListener {
        void configurationChanged();
    }

    public interface OnMenuItemClickListener {
        void onChatMenuItemClicked(int itemId);
    }
}
