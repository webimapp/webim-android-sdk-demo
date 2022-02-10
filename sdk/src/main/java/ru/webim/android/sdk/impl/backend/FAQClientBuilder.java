package ru.webim.android.sdk.impl.backend;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.webim.android.sdk.BuildConfig;
import ru.webim.android.sdk.impl.items.FAQCategoryItem;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FAQClientBuilder {
    private static final int FAQ_TIMEOUT_IN_SECONDS = 30;
    private static final String USER_AGENT_FORMAT = "Android: Webim-Client/%s (%s; Android %s)";
    private static final String USER_AGENT_STRING
            = String.format(USER_AGENT_FORMAT, BuildConfig.VERSION_NAME,
            Build.MODEL, Build.VERSION.RELEASE);
    private String baseUrl;
    private Executor callbackExecutor;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;


    public FAQClientBuilder() {

    }

    public FAQClientBuilder setBaseUrl(@NonNull String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public FAQClientBuilder setCallbackExecutor(@Nullable Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
        return this;
    }

    private static FAQService setupFAQService(String url,
                                              SSLSocketFactory sslSocketFactory,
                                              X509TrustManager trustManager) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .client(setupHttpClient(sslSocketFactory, trustManager))
                .addConverterFactory(GsonConverterFactory.create(setupGson()))
                .build()
                .create(FAQService.class);
    }

    private static OkHttpClient setupHttpClient(final SSLSocketFactory sslSocketFactory,
                                                final X509TrustManager trustManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT_STRING)
                        .build();
                return chain.proceed(request);
            }
        });
        builder.readTimeout(FAQ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        if (sslSocketFactory != null && trustManager != null) {
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }

        return builder.build();
    }

    public FAQClientBuilder setSslSocketFactoryAndTrustManager(SSLSocketFactory sslSocketFactory,
                                                               X509TrustManager trustManager) {
        this.sslSocketFactory = sslSocketFactory;
        this.trustManager = trustManager;
        return this;
    }

    public FAQClient build() {
        if (baseUrl == null) {
            throw new IllegalStateException("baseUrl, location, deltaCallback, platform, title " +
                    "and errorListener must be set to non-null value.");
        }
        if (callbackExecutor == null) {
            throw new IllegalStateException("callbackExecutor must be set to non-null value.");
        }

        FAQService service = setupFAQService(baseUrl, sslSocketFactory, trustManager);
        FAQRequestLoop requestLoop = new FAQRequestLoop(callbackExecutor);

        return new FAQClientImpl(requestLoop, new FAQActions(service, requestLoop));
    }

    private static Gson setupGson() {
        return new GsonBuilder()
                .registerTypeAdapter(FAQCategoryItem.ChildItem.class, new FAQChildDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    private static class FAQClientImpl implements FAQClient {
        @NonNull
        private final FAQActions actions;
        @NonNull
        private final FAQRequestLoop faqRequestLoop;

        private FAQClientImpl(@NonNull FAQRequestLoop faqRequestLoop,
                              @NonNull FAQActions actions) {
            this.faqRequestLoop = faqRequestLoop;
            this.actions = actions;
        }

        @Override
        public void start() {
            faqRequestLoop.start();
        }

        @Override
        public void pause() {
            faqRequestLoop.pause();
        }

        @Override
        public void resume() {
            faqRequestLoop.resume();
        }

        @Override
        public void stop() {
            faqRequestLoop.stop();
        }

        @NonNull
        @Override
        public FAQActions getActions() {
            return actions;
        }
    }
}
