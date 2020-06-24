package com.gemini.jalen.rxservice.download;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Downloader implements Interceptor {
    private ProgressListener listener;
    private Request request;
    private OkHttpClient client;
    private File targetFile;
    private Handler handler;
    private long fileSize;

    public Downloader(ProgressListener listener, long fileSize) {
        this(listener, fileSize, new OkHttpClient.Builder().build());
    }

    public Downloader(ProgressListener listener, long fileSize, OkHttpClient client) {
        this.listener = listener;
        this.fileSize = fileSize;
        this.handler = new Handler(Looper.getMainLooper());
        this.client = client.newBuilder().addNetworkInterceptor(this).build();
    }

    public void setUrl(String url) {
        this.request = new Request.Builder().url(url).build();
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void start() {
        if (!targetFile.getParentFile().exists()) {
            boolean canWrite = targetFile.getParentFile().mkdirs();
            if (!canWrite) {
                throw new RuntimeException("enen");
            }
        }
        handler.post(() -> listener.onStart());
        client.newCall(this.request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onFailed());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                InputStream inputStream;
                OutputStream outputStream = null;
                try {
                    byte[] buffer = new byte[4096];
                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(targetFile);
                    while (true) {
                        int read = inputStream.read(buffer);
                        if (read == -1) {
                            break;
                        }
                        outputStream.write(buffer, 0, read);
                    }

                } catch (Exception e) {
                    onFailure(call, (IOException) e);
                } finally {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        });
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), listener, handler, fileSize))
                .build();
    }
}
