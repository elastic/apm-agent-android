/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.internal.opamp.connectivity.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OkHttpSender implements HttpSender {
  private final OkHttpClient client;
  private final String url;

  public static OkHttpSender create(String url) {
    return create(url, new OkHttpClient());
  }

  public static OkHttpSender create(String url, OkHttpClient client) {
    return new OkHttpSender(url, client);
  }

  private static final String CONTENT_TYPE = "application/x-protobuf";
  private static final MediaType MEDIA_TYPE = MediaType.parse(CONTENT_TYPE);

  private OkHttpSender(String url, OkHttpClient client) {
    this.url = url;
    this.client = client;
  }

  @Override
  public CompletableFuture<Response> send(BodyWriter writer, int contentLength) {
    CompletableFuture<Response> future = new CompletableFuture<>();
    okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
    builder.addHeader("Content-Type", CONTENT_TYPE);

    RequestBody body = new RawRequestBody(writer, contentLength, MEDIA_TYPE);
    builder.post(body);

    client
        .newCall(builder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onResponse(@Nonnull Call call, @Nonnull okhttp3.Response response) {
                future.complete(new OkHttpResponse(response));
              }

              @Override
              public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
                future.completeExceptionally(e);
              }
            });

    return future;
  }

  private static class OkHttpResponse implements Response {
    private final okhttp3.Response response;

    private OkHttpResponse(okhttp3.Response response) {
      this.response = response;
    }

    @Override
    public int statusCode() {
      return response.code();
    }

    @Override
    public String statusMessage() {
      return response.message();
    }

    @Override
    public InputStream bodyInputStream() {
      return response.body().byteStream();
    }

    @Override
    public String getHeader(String name) {
      return response.headers().get(name);
    }

    @Override
    public void close() {
      response.close();
    }
  }

  private static class RawRequestBody extends RequestBody {
    private final BodyWriter writer;
    private final int contentLength;
    private final MediaType contentType;

    private RawRequestBody(BodyWriter writer, int contentLength, MediaType contentType) {
      this.writer = writer;
      this.contentLength = contentLength;
      this.contentType = contentType;
    }

    @Nullable
    @Override
    public MediaType contentType() {
      return contentType;
    }

    @Override
    public long contentLength() {
      return contentLength;
    }

    @Override
    public void writeTo(@Nonnull BufferedSink bufferedSink) throws IOException {
      writer.writeTo(bufferedSink.outputStream());
    }
  }
}
