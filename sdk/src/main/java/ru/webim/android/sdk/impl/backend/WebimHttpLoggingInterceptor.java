package ru.webim.android.sdk.impl.backend;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

/**
 * Created by Nikita Kaberov on 17.07.2018.
 */

public class WebimHttpLoggingInterceptor implements Interceptor {

    public interface Logger {
        void log(String message);
    }


    public WebimHttpLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    private final Logger logger;

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response response = chain.proceed(chain.request());
        try {
            ResponseBody responseBody = response.body();
            Headers headers = response.headers();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();

            if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                GzipSource gzippedResponseBody = null;
                try {
                    gzippedResponseBody = new GzipSource(buffer.clone());
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                } finally {
                    if (gzippedResponseBody != null) {
                        gzippedResponseBody.close();
                    }
                }
            }

            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(Charset.forName("UTF-8"));
            }

            if (responseBody.contentLength() != 0) {
                try {
                    String message = buffer.clone().readString(charset);
                    JSONObject jsonObject = new JSONObject(message);
                    message = "JSON:"
                            + System.getProperty("line.separator")
                            + jsonObject.toString(2);
                    logger.log(message);
                } catch (JSONException ignored) { }
            }
        } catch (OutOfMemoryError ignored) { }

        return response;
    }

}
