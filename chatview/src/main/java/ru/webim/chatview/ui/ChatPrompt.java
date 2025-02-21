package ru.webim.chatview.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;

import ru.webim.android.sdk.MessageTracker;
import ru.webim.chatview.R;
import ru.webim.chatview.utils.ViewUtils;

public class ChatPrompt extends AppCompatTextView implements MessageTracker.MessagesSyncedListener {
    private static final long ANIMATION_DURATION = 500;
    private ConnectionListener connectionListener;
    private State state = State.HIDDEN;
    private boolean syncing = true;

    public ChatPrompt(Context context) {
        this(context, null);
    }

    public ChatPrompt(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatPrompt(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setGravity(Gravity.CENTER);
        setMaxLines(getResources().getInteger(R.integer.chv_notification_bar_max_lines));
        setTextSize(16f);
        int padding = 8;
        setPadding(padding, padding, padding, padding);
        setElevation(5f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        connectionListener = new ConnectionListener();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(connectionListener, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (connectionListener != null) {
            getContext().unregisterReceiver(connectionListener);
        }
    }

    public void setState(State state) {
        State prevState = this.state;
        this.state = state;
        if (prevState == state) {
            return;
        }

        AnimatorSet animatorSet = new AnimatorSet();

        int currentColor = getCurrentColor();
        int nextColor = currentColor;
        List<Animator> animators = new ArrayList<>();
        switch (state) {
            case SYNCING: {
                setVisibility(View.VISIBLE);
                setTextColor(Color.BLACK);
                setText(getContext().getString(R.string.wait_sync_toast));
                nextColor = ViewUtils.resolveAttr(R.attr.chv_prompt_syncing, getContext());
                break;
            }
            case CONN_LOST: {
                setVisibility(View.VISIBLE);
                setTextColor(Color.WHITE);
                setText(getContext().getString(R.string.no_internet_toast));
                nextColor = ViewUtils.resolveAttr(R.attr.chv_prompt_net_lost, getContext());
                break;
            }
            case CONN_LOOSING: {
                setVisibility(View.VISIBLE);
                setTextColor(Color.WHITE);
                setText(getContext().getString(R.string.weak_internet_toast));
                nextColor = ViewUtils.resolveAttr(R.attr.chv_prompt_net_losing, getContext());
            }
            case HIDDEN: {
                ObjectAnimator translationAnim = ObjectAnimator.ofFloat(this, "translationY", -getHeight());
                translationAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setVisibility(View.GONE);
                    }
                });
                animators.add(translationAnim);
                break;
            }
        }

        if (prevState != State.HIDDEN) {
            ValueAnimator backgroundAmin = ValueAnimator.ofArgb(currentColor, nextColor);
            backgroundAmin.addUpdateListener(animation -> setBackgroundColor((Integer) animation.getAnimatedValue()));
            animators.add(backgroundAmin);
        } else {
            setBackgroundColor(nextColor);
            ObjectAnimator translationAnim = ObjectAnimator.ofFloat(this, "translationY", -getHeight(), 0);
            translationAnim.setInterpolator(new LinearInterpolator());
            animators.add(translationAnim);
        }

        animatorSet.setDuration(ANIMATION_DURATION).playTogether(animators);
        animatorSet.start();
    }

    private int getCurrentColor() {
        int currentColor;
        ColorDrawable drawable = ((ColorDrawable) getBackground());
        if (drawable == null) {
            currentColor = ViewUtils.resolveAttr(R.attr.chv_prompt_syncing, getContext());
        } else {
            currentColor = drawable.getColor();
        }
        return currentColor;
    }

    @Override
    public void messagesSynced() {
        syncing = false;
        setState(State.HIDDEN);
    }

    public enum State {
        SYNCING, CONN_LOST, CONN_LOOSING, HIDDEN;
    }

    private class ConnectionListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            post(() -> {
                Log.d("AAAAA", "connected: " + isConnected);
                if (isConnected) {
                    if (!syncing) {
                        setState(State.HIDDEN);
                    } else {
                        setState(State.SYNCING);
                    }
                } else {
                    setState(State.CONN_LOST);
                }
            });
        }
    }
}
