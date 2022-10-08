# server-sdk-kotlin

Kotlin SDK for accessing [livekit-server](https://github.com/livekit/livekit) APIs.

https://docs.livekit.io/guides/server-api/

## Installation

This SDK is available as a Maven package through [JitPack](https://jitpack.io/#livekit/server-sdk-kotlin).

```groovy title="build.gradle"

repositories {
	...
	maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'com.github.livekit:server-sdk-kotlin:<commit hash>'
}
```

## Usage

Obtain a `RoomServiceClient` or `EgressServiceClient` through their respective `create` methods, and then run calls through the client.

```java
package org.example;

import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import retrofit2.Call;
import retrofit2.Response;

public class Main {
  public static void main(String[] args) throws IOException {

    RoomServiceClient client = RoomServiceClient.create(
            "http://example.com", 
            "apiKey",
            "secret");

    Call<LivekitModels.Room> call = client.createRoom("room_name");
    Response<LivekitModels.Room> response = call.execute();
    LivekitModels.Room room = response.body();

    System.out.println(JsonFormat.printer().print(room));
  }
}
```

`Call` adapters are also available through [Retrofit](https://github.com/square/retrofit/tree/master/retrofit-adapters) for other async constructs such as `CompletableFuture` and RxJava.
