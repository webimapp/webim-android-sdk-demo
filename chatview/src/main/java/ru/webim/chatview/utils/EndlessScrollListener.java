package ru.webim.chatview.utils;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private static final int DEFAULT_THRESHOLD = 5;
    private final int visibleThreshold;
    private boolean loading = false;
    private FloatingActionButton downButton = null;
    private RecyclerView.Adapter<?> adapter = null;

    public EndlessScrollListener() {
        this(DEFAULT_THRESHOLD);
    }

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (adapter == null || dy == 0) return;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        if (downButton != null) {
            if (layoutManager.findFirstVisibleItemPosition() >= visibleThreshold) {
                downButton.setVisibility(View.VISIBLE);
            } else {
                downButton.setVisibility(View.GONE);
            }
        }

        if (!loading
                && layoutManager.findLastVisibleItemPosition() >= adapter.getItemCount() - visibleThreshold
                && dy < 0) {
            loading = true;
            onLoadMore(adapter.getItemCount());
        }
    }

    public void setDownButton(FloatingActionButton downButton) {
        this.downButton = downButton;
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter = adapter;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public abstract void onLoadMore(int totalItemsCount);
}
