package com.gemini.jalen.rxservice.download;

import android.os.Handler;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ProgressResponseBody  extends ResponseBody {
    private final ResponseBody responseBody;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;
    private Handler handler;
    private long fileSize;

    ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener, Handler handler, long fileSize) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
        this.handler = handler;
        this.fileSize = fileSize;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return fileSize == 0 ? responseBody.contentLength() : fileSize;
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long currentSize = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long size = super.read(sink, byteCount);
                handler.post(() -> progressListener.update(currentSize += size != -1 ? size : 0, contentLength(), size == -1));
                return size;
            }
        };
    }
}
