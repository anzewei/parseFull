package com.github.anzewei.parsefull;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * ParseFull
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since 1.0.0
 */

public class ParseFull {
    public final static int CODE_CANCEL = -2333;
    public final static int CODE_HTTP_ERROR = -2331;
    private Call mCall;

    private ParseResult mParseResult;

    public Context getContext() {
        return mContext;
    }

    public Object getTag() {
        return tag;
    }

    public static class ParseResult {
        public int status;
        public Object result;
    }

    private AtomicBoolean mCancel = new AtomicBoolean(false);
    private List<NetParser> mNetParsers;
    private NetDisplay mNetDisplay;
    private ResultHandler mResultHandler;
    private Request mRequest;
    private Callback mCallback;
    private Context mContext;
    private Object tag;

    private ParseFull(ParseBuilder builder) {
        Request.Builder requesBuilder = new Request.Builder();
        requesBuilder.url(builder.url);
        requesBuilder.method(builder.method, builder.body);
        requesBuilder.headers(builder.headers.build());
        requesBuilder.tag(builder.tag);
        mRequest = requesBuilder.build();
        tag = builder.tag;
        mContext = builder.mContext;
        mNetDisplay = builder.mNetDisplay;
        mNetParsers = builder.mNetParser;
        mResultHandler = builder.mResultHandler;
    }


    private void entureCallback() {
        if (mCallback == null)
            mCallback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onHttpGet(CODE_HTTP_ERROR, null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    onHttpGet(response.code(), response.body());
                }
            };
    }

    private void onHttpGet(int status, ResponseBody result) {
        mParseResult = new ParseResult();
        mParseResult.result = result;
        mParseResult.status = status;
        if (mNetParsers != null) {
            for (NetParser netParser : mNetParsers) {
                if (!netParser.parse(mParseResult))
                    break;
            }
        }
        if (mCancel.get()) {
            mParseResult.status = CODE_CANCEL;
        }
        if (mNetDisplay != null) {
            mNetDisplay.onPostExecute(this);
        }
        if (mResultHandler != null)
            mResultHandler.onParseFinish(this, mParseResult);
    }

    public void cancel() {
        mCancel.set(true);
        if (mCall != null)
            mCall.cancel();
    }

    public void execute(OkHttpClient client) {
        if (mNetDisplay != null) {
            mNetDisplay.onPreExecute(this);
        }
        entureCallback();
        mCall = client.newCall(mRequest);
        mCall.enqueue(mCallback);
    }

    public Object executeSync(OkHttpClient client) throws IOException {

        if (mNetDisplay != null) {
            mNetDisplay.onPostExecute(this);
        }
        Response response = client.newCall(mRequest).execute();
        onHttpGet(response.code(), response.body());
        return mParseResult;
    }


    public static class ParseBuilder {

        private NetDisplay mNetDisplay;
        private ArrayList<NetParser> mNetParser;
        private ResultHandler mResultHandler;
        private Context mContext;
        private HashMap<String, String> params;
        private HashMap<String, RequestBody> files;
        private HttpUrl url;
        private String method;
        private Headers.Builder headers;
        private RequestBody body;
        private Object tag;

        public ParseBuilder(Context context) {
            this.method = "GET";
            mContext = context;
            this.headers = new Headers.Builder();
        }

        public ParseBuilder url(HttpUrl url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        /**
         * Sets the URL target of this
         *
         * @throws IllegalArgumentException if {@code url} is not a valid HTTP or HTTPS URL. Avoid this
         *                                  exception by calling {@link HttpUrl#parse}; it returns null for invalid URLs.
         */
        public ParseBuilder url(String url) {
            if (url == null) throw new NullPointerException("url == null");

            // Silently replace websocket URLs with HTTP URLs.
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }

            HttpUrl parsed = HttpUrl.parse(url);
            if (parsed == null) throw new IllegalArgumentException("unexpected url: " + url);
            return url(parsed);
        }

        /**
         * Sets the URL target of this
         *
         * @throws IllegalArgumentException if the scheme of {@code url} is not {@code http} or {@code
         *                                  https}.
         */
        public ParseBuilder url(URL url) {
            if (url == null) throw new NullPointerException("url == null");
            HttpUrl parsed = HttpUrl.get(url);
            if (parsed == null) throw new IllegalArgumentException("unexpected url: " + url);
            return url(parsed);
        }

        /**
         * Sets the header named {@code name} to {@code value}. If this request already has any headers
         * with that name, they are all replaced.
         */
        public ParseBuilder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
         * headers like "Cookie".
         * <p>
         * <p>Note that for some headers including {@code Content-Length} and {@code Content-Encoding},
         * OkHttp may replace {@code value} with a header derived from the request body.
         */
        public ParseBuilder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        public ParseBuilder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * Removes all headers on this builder and adds {@code headers}.
         */
        public ParseBuilder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        /**
         * Sets this request's {@code Cache-Control} header, replacing any cache control headers already
         * present. If {@code cacheControl} doesn't define any directives, this clears this request's
         * cache-control headers.
         */
        public ParseBuilder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty()) return removeHeader("Cache-Control");
            return header("Cache-Control", value);
        }

        public ParseBuilder get() {
            method = "GET";
            return this;
        }

        public ParseBuilder post() {
            method = "POST";
            return this;
        }

        public ParseBuilder head() {
            return method("HEAD", null);
        }


        public ParseBuilder delete(RequestBody body) {
            return method("DELETE", body);
        }

        public ParseBuilder delete() {
            return delete(RequestBody.create(null, new byte[0]));
        }

        public ParseBuilder put(RequestBody body) {
            return method("PUT", body);
        }

        public ParseBuilder patch(RequestBody body) {
            return method("PATCH", body);
        }

        public ParseBuilder method(String method, RequestBody body) {
            if (method == null) throw new NullPointerException("method == null");
            if (method.length() == 0) throw new IllegalArgumentException("method.length() == 0");
            if (body != null && !HttpMethod.permitsRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            }
            if (body == null && HttpMethod.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            }
            this.method = method;
            this.body = body;
            return this;
        }

        public ParseBuilder tag(Object tag) {
            this.tag = tag;
            return this;
        }


        public ParseBuilder displayer(NetDisplay netDisplay) {
            mNetDisplay = netDisplay;
            return this;
        }

        public ParseBuilder resultHandler(ResultHandler handler) {
            mResultHandler = handler;
            return this;
        }

        public ParseBuilder addNetParser(NetParser netParser) {
            if (mNetParser == null)
                mNetParser = new ArrayList<>();
            mNetParser.add(netParser);
            return this;
        }

        public ParseBuilder addParams(String key, String value) {
            if (params == null)
                params = new HashMap<>();
            this.params.put(key, value);
            return this;
        }

        public ParseBuilder addFile(String key, final String mediaType, final InputStream value) {
            if (this.files == null)
                files = new HashMap<>();
            files.put(key, new FileBody(MediaType.parse(mediaType), value));
            return this;
        }

        public ParseFull build() {
            if (url == null) throw new IllegalStateException("url == null");
            if (method.equalsIgnoreCase("post")) {
                if (files != null) {
                    body = createFileBody();
                } else if (params != null) body = createStringBody();
            } else if (method.equalsIgnoreCase("get") && params != null) {
                HttpUrl.Builder builder = url.newBuilder();
                Iterator iter = params.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) iter.next();
                    String key = entry.getKey();
                    String val = entry.getValue();
                    builder.setQueryParameter(key,val);
                }
                url(builder.build());
            }
            return new ParseFull(this);
        }

        private RequestBody createFileBody() {
            MultipartBody.Builder postBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (params != null) {
                Iterator iter = params.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) iter.next();
                    String key = entry.getKey();
                    String val = entry.getValue();
                    postBuilder.addFormDataPart(key, val);
                }
            }
            if (files != null) {
                Iterator iter = files.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, FileBody> entry = (Map.Entry) iter.next();
                    String key = entry.getKey();
                    FileBody val = entry.getValue();
                    postBuilder.addFormDataPart(key, "filename", val);
                }
            }
            return postBuilder.build();
        }

        private RequestBody createStringBody() {
            FormBody.Builder postBuilder = new FormBody.Builder();
            if (params != null) {
                Iterator iter = params.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) iter.next();
                    String key = entry.getKey();
                    String val = entry.getValue();
                    postBuilder.add(key, val);
                }
            }
            return postBuilder.build();
        }
    }

    public static class FileBody extends RequestBody {

        private MediaType mMediaType;
        private InputStream mStream;

        public FileBody(MediaType mediaType, InputStream stream) {
            mMediaType = mediaType;
            mStream = stream;
        }

        @Override
        public MediaType contentType() {
            return mMediaType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            try {
                source = Okio.source(mStream);
                sink.writeAll(source);
            } finally {
                Util.closeQuietly(source);
            }
        }

        @Override
        public long contentLength() throws IOException {
            return mStream.available();
        }
    }
}

