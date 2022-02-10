package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.Webim;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import retrofit2.Call;

public class FAQRequestLoop extends AbstractRequestLoop {
    private final BlockingQueue<FAQRequestLoop.WebimRequest> queue = new ArrayBlockingQueue<>(128);
    @Nullable
    private FAQRequestLoop.WebimRequest<?> lastRequest;

    public FAQRequestLoop(@NonNull Executor callbackExecutor) {
        super(callbackExecutor, null);
    }

    @Override
    protected void run() {
        try {
            while (isRunning()) {
                try {
                    if (!isRunning()) {
                        return;
                    }
                    runIteration();
                } catch (final AbortByWebimErrorException e) {
                    if (WebimInternalError.WRONG_ARGUMENT_VALUE.equals(e.getError())) {
                        WebimInternalLog.getInstance().log("Error: " + "\""+ e.getError() + "\""
                                        + ", argumentName: " + "\"" + e.getArgumentName() + "\"",
                                Webim.SessionBuilder.WebimLogVerbosityLevel.ERROR);
                        this.lastRequest = null;
                    } else {
                        running = false;
                    }
                } catch (InterruptedRuntimeException ignored) { }
            }
        } catch (final Throwable t) {
            running = false;

            throw t;
        }
    }

    @SuppressWarnings("unchecked")
    private void runIteration() {
        FAQRequestLoop.WebimRequest<?> currentRequest = this.lastRequest;
        if (currentRequest == null) {
            try {
                this.lastRequest = currentRequest = queue.take();
            } catch (InterruptedException e) {
                return;
            }
        }

        try {
            performRequestAndCallback(currentRequest);
        } catch (AbortByWebimErrorException exception) {
            if ((exception.getError() != null)
                    && currentRequest.isHandleError(exception.getError())) {
                if (currentRequest.hasCallback) {
                    final FAQRequestLoop.WebimRequest<?> callback = currentRequest;
                    final String error = exception.getError();
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.handleError(error);
                        }
                    });
                } // Else ignore.
            } else {
                throw exception;
            }
        }

        this.lastRequest = null;
    }

    private <T> void performRequestAndCallback(FAQRequestLoop.WebimRequest<T> currentRequest) {
        final T response = performFAQRequest(currentRequest.makeRequest());

        if (currentRequest.hasCallback) {
            final FAQRequestLoop.WebimRequest<T> callback = currentRequest;
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.runCallback(response);
                }
            });
        }
    }

    void enqueue(FAQRequestLoop.WebimRequest<?> request) {
        try {
            queue.put(request);
        } catch (InterruptedException ignored) { }
    }

    static abstract class WebimRequest<T> {
        private final boolean hasCallback;

        protected WebimRequest(boolean hasCallback) {
            this.hasCallback = hasCallback;
        }

        public abstract Call<T> makeRequest();

        public void runCallback(T response) { }

        public boolean isHandleError(@NonNull String error) {
            return false;
        }

        public void handleError(@NonNull String error) { }
    }
}
