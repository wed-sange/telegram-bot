package com.sange.telegram.bot.util.http;

import com.sange.telegram.bot.util.http.exception.HttpIOException;
import com.sange.telegram.bot.util.http.exception.HttpResponseException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
    private OkHttpClient client;

    public HttpClient(OkHttpClient client) {
        this.client = client;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public String request(Request request) {
        byte[] data = call(request);
        return byteToString(data);
    }

    public byte[] call(Request request) {
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new HttpIOException("http请求异常,url = " + request.url(), e);
        }

        byte[] bytes;
        try {
            bytes = response.body().bytes();
        } catch (IOException e) {
            throw new HttpIOException("http获取body异常,url = " + request.url(), e);
        }

        if (response.isSuccessful()) {
            return bytes;
        } else {
            String content = new String(bytes, StandardCharsets.UTF_8);
            String errMsg = new StringBuilder()
                    .append("http响应异常,url= ").append(request.url())
                    .append(", responseCode=").append(response.code())
                    .append(", body = ").append(content).toString();
            throw new HttpResponseException(response.code(), errMsg);
        }
    }

    private String byteToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void call(Request request, Callback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
                response.close();
            }
        });
    }

    public String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return byteToString(call(request));
    }

    public String get(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).get();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        Request request = builder.build();
        return byteToString(call(request));
    }


    public byte[] getAsBytes(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return call(request);
    }

    public String postJson(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return byteToString(call(request));
    }

    public String postText(String url, String text) {
        RequestBody body = RequestBody.create(TEXT, text);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return byteToString(call(request));
    }


    public String postForm(String url, Map<String, String> params) {
        FormBody.Builder formBody = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBody.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBody.build())
                .build();
        return byteToString(call(request));
    }

}
