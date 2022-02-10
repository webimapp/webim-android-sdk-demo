package ru.webim.android.demo.client.util;

import android.widget.AbsListView;

/**
 * This scroll listener emulates TRANSCRIPT_MODE_NORMAL more properly. List with default TRANSCRIPT_MODE_NORMAL
 * don't scroll to the end if multiple messages added in same time
 *
 * @see <a href="https://stackoverflow.com/questions/5521442">https://stackoverflow.com/questions/5521442/why-is-androidtranscriptmode-normal-not-working-properly</a>
 */
public class NormalTranscriptModeScrollListener implements AbsListView.OnScrollListener {
    private boolean isScrollingBottom = true;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        isScrollingBottom = false;
    }

    @Override
    public void onScroll(AbsListView view, int first, int visible, int total) {
        if (visible == 0 || visible == total || (first + visible == total)) {
            if (view.getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL) {
                view.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                isScrollingBottom = true;
            }
        } else if (!isScrollingBottom) {
            if (view.getTranscriptMode() != AbsListView.TRANSCRIPT_MODE_DISABLED)
                view.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        }
    }
}
