<!--BEGIN_BANNER_IMAGE-->

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="/.github/banner_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="/.github/banner_light.png">
  <img style="width:100%;" alt="The LiveKit icon, the name of the repository and some sample code in the background." src="https://raw.githubusercontent.com/livekit/server-sdk-kotlin/main/.github/banner_light.png">
</picture>

<!--END_BANNER_IMAGE-->

# LiveKit Server SDK for Kotlin

<!--BEGIN_DESCRIPTION-->
Use this SDK to interact with <a href="https://livekit.io/">LiveKit</a> server APIs and create access tokens from your Kotlin backend.
<!--END_DESCRIPTION-->

## Installation

This SDK is available as a Maven package through [Maven Central](https://search.maven.org/search?q=g:io.livekit%20a:livekit-server).

### Maven

```xml title="pom.xml"

<dependencies>
    <dependency>
        <groupId>io.livekit</groupId>
        <artifactId>livekit-server</artifactId>
        <version>0.12.1</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy title="build.gradle"
dependencies {
    implementation 'io.livekit:livekit-server:0.12.1'
}
```

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

    RoomServiceClient client = RoomServiceClient.createClient(
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
<tr><td>Agents SDKs</td><td><a href="https://github.com/livekit/agents">Python</a> · <a href="https://github.com/livekit/agents-js">Node.js</a></td></tr><tr></tr>
<tr><td>LiveKit SDKs</td><td><a href="https://github.com/livekit/client-sdk-js">Browser</a> · <a href="https://github.com/livekit/client-sdk-swift">Swift</a> · <a href="https://github.com/livekit/client-sdk-android">Android</a> · <a href="https://github.com/livekit/client-sdk-flutter">Flutter</a> · <a href="https://github.com/livekit/client-sdk-react-native">React Native</a> · <a href="https://github.com/livekit/rust-sdks">Rust</a> · <a href="https://github.com/livekit/node-sdks">Node.js</a> · <a href="https://github.com/livekit/python-sdks">Python</a> · <a href="https://github.com/livekit/client-sdk-unity">Unity</a> · <a href="https://github.com/livekit/client-sdk-unity-web">Unity (WebGL)</a> · <a href="https://github.com/livekit/client-sdk-esp32">ESP32</a> · <a href="https://github.com/livekit/client-sdk-cpp">C++</a></td></tr><tr></tr>
<tr><td>Starter Apps</td><td><a href="https://github.com/livekit-examples/agent-starter-python">Python Agent</a> · <a href="https://github.com/livekit-examples/agent-starter-node">TypeScript Agent</a> · <a href="https://github.com/livekit-examples/agent-starter-react">React App</a> · <a href="https://github.com/livekit-examples/agent-starter-swift">SwiftUI App</a> · <a href="https://github.com/livekit-examples/agent-starter-android">Android App</a> · <a href="https://github.com/livekit-examples/agent-starter-flutter">Flutter App</a> · <a href="https://github.com/livekit-examples/agent-starter-react-native">React Native App</a> · <a href="https://github.com/livekit-examples/agent-starter-embed">Web Embed</a></td></tr><tr></tr>
<tr><td>UI Components</td><td><a href="https://github.com/livekit/components-js">React</a> · <a href="https://github.com/livekit/components-android">Android Compose</a> · <a href="https://github.com/livekit/components-swift">SwiftUI</a> · <a href="https://github.com/livekit/components-flutter">Flutter</a></td></tr><tr></tr>
<tr><td>Server APIs</td><td><a href="https://github.com/livekit/node-sdks">Node.js</a> · <a href="https://github.com/livekit/server-sdk-go">Golang</a> · <a href="https://github.com/livekit/server-sdk-ruby">Ruby</a> · <b>Java/Kotlin</b> · <a href="https://github.com/livekit/python-sdks">Python</a> · <a href="https://github.com/livekit/rust-sdks">Rust</a> · <a href="https://github.com/agence104/livekit-server-sdk-php">PHP (community)</a> · <a href="https://github.com/pabloFuente/livekit-server-sdk-dotnet">.NET (community)</a></td></tr><tr></tr>
<tr><td>Resources</td><td><a href="https://docs.livekit.io">Docs</a> · <a href="https://docs.livekit.io/mcp">Docs MCP Server</a> · <a href="https://github.com/livekit/livekit-cli">CLI</a> · <a href="https://cloud.livekit.io">LiveKit Cloud</a></td></tr><tr></tr>
<tr><td>LiveKit Server OSS</td><td><a href="https://github.com/livekit/livekit">LiveKit server</a> · <a href="https://github.com/livekit/egress">Egress</a> · <a href="https://github.com/livekit/ingress">Ingress</a> · <a href="https://github.com/livekit/sip">SIP</a></td></tr><tr></tr>
<tr><td>Community</td><td><a href="https://community.livekit.io">Developer Community</a> · <a href="https://livekit.io/join-slack">Slack</a> · <a href="https://x.com/livekit">X</a> · <a href="https://www.youtube.com/@livekit_io">YouTube</a></td></tr>
</tbody>
</table>
<!--END_REPO_NAV-->
