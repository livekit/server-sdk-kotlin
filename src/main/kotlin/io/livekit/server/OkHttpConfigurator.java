package io.livekit.server;

import java.net.UnknownHostException;

import okhttp3.OkHttpClient;

public interface OkHttpConfigurator {
    void config(OkHttpClient.Builder builder) throws UnknownHostException;
}
