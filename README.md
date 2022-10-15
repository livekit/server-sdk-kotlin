# server-sdk-kotlin

Kotlin SDK for accessing [livekit-server](https://github.com/livekit/livekit) APIs. Currently for use in JVM environments.

https://docs.livekit.io/guides/server-api/

## Installation

This SDK is available as a Maven package through [Maven Central](https://search.maven.org/search?q=g:io.livekit%20a:livekit-server).

### Maven
```xml title="pom.xml"

<dependencies>
    <dependency>
        <groupId>io.livekit</groupId>
        <artifactId>livekit-server</artifactId>
        <version>97c8779b09</version>
    </dependency>
</dependencies>
```

### Gradle
```groovy title="build.gradle"

repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.livekit:server-sdk-kotlin:<commit hash>'
}
```

Development snapshots are available through Sonatype: `https://s01.oss.sonatype.org/content/repositories/snapshots/`

## Usage

### Server API Access

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
    Response<LivekitModels.Room> response = call.execute(); // Use call.enqueue for async
    LivekitModels.Room room = response.body();

    System.out.println(JsonFormat.printer().print(room));
  }
}
```

`Call` adapters are also available through [Retrofit](https://github.com/square/retrofit/tree/master/retrofit-adapters) for other async constructs such as `CompletableFuture` and RxJava.


### Creating Access Tokens

Access tokens can be generated through the `io.livekit.server.AccessToken` class.

```
AccessToken token = new AccessToken("apiKey", "secret");

// Fill in token information.
token.setName("name");
token.setIdentity("identity");
token.setMetadata("metadata");
token.addGrants(new RoomJoin(true), new RoomName("myroom"));

// Sign and create token string.
System.out.println("New access token: " + token.toJwt())
```

By default, tokens expire 6 hours after generation. You may override this by using `token.setTtl(long millis)`.
    
