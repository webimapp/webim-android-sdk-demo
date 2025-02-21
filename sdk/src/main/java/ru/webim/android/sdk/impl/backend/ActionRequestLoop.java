package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.webim.android.sdk.NotFatalErrorHandler;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.impl.items.responses.ErrorResponse;

import java.io.FileNotFoundException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import retrofit2.Call;

public class ActionRequestLoop extends AbstractRequestLoop {
    private final BlockingQueue<WebimRequest<?>> queue = new ArrayBlockingQueue<>(128);
    @Nullable
    private volatile AuthData authData;
    @Nullable
    private WebimRequest<?> lastRequest;

    public ActionRequestLoop(@NonNull Executor callbackExecutor,
                             @NonNull InternalErrorListener errorListener) {
        super(callbackExecutor, errorListener);
    }

    public void setAuthData(@Nullable AuthData pageId) {
        this.authData = pageId;
    }

    @Override
    protected void run() {
        try {
            while (isRunning()) {
                AuthData currentAuthData = authData;
                if (currentAuthData == null) {
                    currentAuthData = awaitNewPageId(null);
                }

                try {
                    if (!isRunning()) {
                        return;
                    }

                    runIteration(currentAuthData);
                } catch (final AbortByWebimErrorException e) {
                    if (WebimInternalError.WRONG_ARGUMENT_VALUE.equals(e.getError())) {
                        WebimInternalLog.getInstance().log("Error: " + "\""+ e.getError() + "\""
                                + ", argumentName: " + "\"" + e.getArgumentName() + "\"",
                                Webim.SessionBuilder.WebimLogVerbosityLevel.ERROR);
                        this.lastRequest = null;
                    } else if (WebimInternalError.REINIT_REQUIRED.equals(e.getError())) {
                        awaitNewPageId(currentAuthData);
                    } else {
                        running = false;

                        callbackExecutor.execute(() -> errorListener.onError(
                                e.getRequest().request().url().toString(),
                                e.getError(),
                                e.getHttpCode()
                        ));
                    }
                } catch (InterruptedIOException e) {
                    WebimInternalLog.getInstance().log(e.toString(),
                            Webim.SessionBuilder.WebimLogVerbosityLevel.DEBUG);
                    final WebimRequest<?> request = lastRequest;
                    callbackExecutor.execute(() -> {
                        if (request != null) {
                            request.handleError(NotFatalErrorHandler.NotFatalErrorType.SOCKET_TIMEOUT_EXPIRED.toString());
                        }
                    });
                    this.lastRequest = null;
                } catch (InterruptedRuntimeException ignored) { }
            }
        } catch (final Throwable t) {
            running = false;

            throw t;
        }
    }

    private AuthData awaitNewPageId(@Nullable AuthData lastAuthData) {
        //noinspection StringEquality
        while (isRunning() && lastAuthData == authData) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) { }
        }
        return authData;
    }

    private void runIteration(AuthData currentAuthData) throws InterruptedIOException {
        WebimRequest<?> currentRequest = this.lastRequest;
        if (currentRequest == null) {
            try {
                this.lastRequest = currentRequest = queue.take();
            } catch (InterruptedException e) {
                return;
            }
        }

        try {
            performRequestAndCallback(currentAuthData, currentRequest);
        } catch (final FileNotFoundException exception) {
            final WebimRequest<?> callback = currentRequest;
            callbackExecutor.execute(() -> callback.handleError(WebimInternalError.FILE_NOT_FOUND));
        } catch (InterruptedIOException exception) {
            if (exception instanceof SocketTimeoutException) {
                throw exception;
            } else {
                final WebimRequest<?> callback = currentRequest;
                callbackExecutor.execute(() -> callback.handleError(WebimInternalError.CONNECTION_TIMEOUT));
            }
        } catch (AbortByWebimErrorException exception) {
            if ((exception.getError() != null)
                    && currentRequest.isHandleError(exception.getError())) {
                if (currentRequest.hasCallback) {
                    final WebimRequest<?> callback = currentRequest;
                    final String error = exception.getError();
                    callbackExecutor.execute(() -> callback.handleError(error));
                } // Else ignore.
            } else {
                throw exception;
            }
        }

        this.lastRequest = null;
    }

    private <T extends ErrorResponse> void performRequestAndCallback(
        AuthData currentAuthData, WebimRequest<T> currentRequest) throws InterruptedIOException, FileNotFoundException {
        final T response = performRequest(currentRequest.makeRequest(currentAuthData));

        if (currentRequest.hasCallback) {
            final WebimRequest<T> callback = currentRequest;
            callbackExecutor.execute(() -> callback.runCallback(response));
        }
    }

    void enqueue(WebimRequest<?> request) {
        try {
            queue.put(request);
        } catch (InterruptedException ignored) { }
    }

    static abstract class WebimRequest<T extends ErrorResponse> {
        private final boolean hasCallback;

        protected WebimRequest(boolean hasCallback) {
            this.hasCallback = hasCallback;
        }

        public abstract Call<T> makeRequest(AuthData authData);

        public void runCallback(T response) { }

        public boolean isHandleError(@NonNull String error) {
            return false;
        }

        public void handleError(@NonNull String error) { }
    }
}
