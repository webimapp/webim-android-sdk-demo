package ru.webim.android.demo.client.util;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private int visibleThreshold;
    private boolean loading = false;
    private FloatingActionButton downButton = null;
    private RecyclerView.Adapter adapter = null;

    public EndlessScrollListener() {
        this(5);
    }

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (adapter == null) return;
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

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public abstract void onLoadMore(int totalItemsCount);
}
