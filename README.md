<!--BEGIN_BANNER_IMAGE-->
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="/.github/banner_dark.png">
    <source media="(prefers-color-scheme: light)" srcset="/.github/banner_light.png">
    <img style="width:100%;" alt="The LiveKit icon, the name of the repository and some sample code in the background." src="/.github/banner_light.png">
  </picture>
  <!--END_BANNER_IMAGE-->

# server-sdk-kotlin

<!--BEGIN_DESCRIPTION-->Use this SDK to manage <a href="https://livekit.io/">LiveKit</a> rooms and create access tokens from your Kotlin backend.<!--END_DESCRIPTION-->

https://docs.livekit.io/guides/server-api/

## Installation

This SDK is available as a Maven package through [Maven Central](https://search.maven.org/search?q=g:io.livekit%20a:livekit-server).

### Maven
```xml title="pom.xml"

<dependencies>
    <dependency>
        <groupId>io.livekit</groupId>
        <artifactId>livekit-server</artifactId>
        <version>0.5.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy title="build.gradle"
dependencies {
    implementation 'io.livekit:livekit-server:0.5.0'
}
```

Development snapshots are available through Sonatype: `https://s01.oss.sonatype.org/content/repositories/snapshots/`

The current snapshot `VERSION_NAME` can be found in the root
folder's [gradle.properties](https://github.com/livekit/server-sdk-kotlin/blob/main/gradle.properties).

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

```java
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
    
<!--BEGIN_REPO_NAV-->
<br/><table>
<thead><tr><th colspan="2">LiveKit Ecosystem</th></tr></thead>
<tbody>
<tr><td>Client SDKs</td><td><a href="https://github.com/livekit/components-js">Components</a> · <a href="https://github.com/livekit/client-sdk-js">JavaScript</a> · <a href="https://github.com/livekit/client-sdk-rust">Rust</a> · <a href="https://github.com/livekit/client-sdk-swift">iOS/macOS</a> · <a href="https://github.com/livekit/client-sdk-android">Android</a> · <a href="https://github.com/livekit/client-sdk-flutter">Flutter</a> · <a href="https://github.com/livekit/client-sdk-unity-web">Unity (web)</a> · <a href="https://github.com/livekit/client-sdk-react-native">React Native (beta)</a></td></tr><tr></tr>
<tr><td>Server SDKs</td><td><a href="https://github.com/livekit/server-sdk-js">Node.js</a> · <a href="https://github.com/livekit/server-sdk-go">Golang</a> · <a href="https://github.com/livekit/server-sdk-ruby">Ruby</a> · <b>Java/Kotlin</b> · <a href="https://github.com/agence104/livekit-server-sdk-php">PHP (community)</a> · <a href="https://github.com/tradablebits/livekit-server-sdk-python">Python (community)</a></td></tr><tr></tr>
<tr><td>Services</td><td><a href="https://github.com/livekit/livekit">Livekit</a> · <a href="https://github.com/livekit/egress">Egress</a> · <a href="https://github.com/livekit/ingress">Ingress</a></td></tr><tr></tr>
<tr><td>Resources</td><td><a href="https://docs.livekit.io">Docs</a> · <a href="https://github.com/livekit-examples">Example apps</a> · <a href="https://livekit.io/cloud">Cloud</a> · <a href="https://docs.livekit.io/oss/deployment">Self-hosting</a> · <a href="https://github.com/livekit/livekit-cli">CLI</a></td></tr>
</tbody>
</table>
<!--END_REPO_NAV-->
