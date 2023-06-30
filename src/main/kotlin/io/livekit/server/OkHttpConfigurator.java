package io.livekit.server;

import okhttp3.OkHttpClient;

public interface OkHttpConfigurator {
    void config(OkHttpClient.Builder builder);
}
