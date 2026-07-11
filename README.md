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
        <version>0.14.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy title="build.gradle"
dependencies {
    implementation 'io.livekit:livekit-server:0.14.0'
}
```

## Usage

### Server API Access

`LiveKitAPI` is a single entry point to every server API, exposing each service (`room`, `egress`, `ingress`, `sip`, `agentDispatch`, `connector`). Create it with `createClient` (API key & secret, for backend use) or `createClientWithToken` (a pre-signed token, for client-side use where the API secret must not be exposed). Host and credentials fall back to the `LIVEKIT_URL`, `LIVEKIT_API_KEY`, `LIVEKIT_API_SECRET`, and `LIVEKIT_TOKEN` environment variables. Values you pass explicitly take precedence; the environment variables are used only as a fallback for arguments you omit — an ambient `LIVEKIT_TOKEN`, for example, won't override an explicitly-provided API key and secret.

```java
package org.example;

import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

import io.livekit.server.LiveKitAPI;
import livekit.LivekitModels;
import retrofit2.Call;
import retrofit2.Response;

public class Main {
  public static void main(String[] args) throws IOException {

    // With LIVEKIT_URL, LIVEKIT_API_KEY, and LIVEKIT_API_SECRET set, call createClient()
    // with no arguments; pass any of them to override the corresponding env var.
    LiveKitAPI api = LiveKitAPI.createClient();
    // explicit:    LiveKitAPI.createClient("http://example.com", "apiKey", "secret");
    // client-side: LiveKitAPI.createClientWithToken("http://example.com", token); // token from LIVEKIT_TOKEN if omitted

    Call<LivekitModels.Room> call = api.getRoom().createRoom("room_name");
    Response<LivekitModels.Room> response = call.execute(); // Use call.enqueue for async
    LivekitModels.Room room = response.body();

    System.out.println(JsonFormat.printer().print(room));
  }
}
```

`Call` adapters are also available through [Retrofit](https://github.com/square/retrofit/tree/master/retrofit-adapters) for other async constructs such as `CompletableFuture` and RxJava. The individual service clients (`RoomServiceClient`, etc.) can also be created directly with the same arguments.

### Error handling

Retrofit doesn't raise on API errors, so a failed call comes back as an unsuccessful response. `ServerError.from(response)` decodes the error code, message, and metadata from it (returns `null` if the response succeeded or isn't a server error):

```java
import io.livekit.server.ServerError;

Response<LivekitModels.Room> response = api.getRoom().createRoom("my-room").execute();
if (!response.isSuccessful()) {
    ServerError error = ServerError.from(response);
    if (error != null) {
        System.out.println(error.getCode() + ": " + error.getMessage());
    }
}
```

A failed SIP dial (e.g. the callee is busy) can be decoded as a `SipCallError` (a `ServerError` subclass) that also exposes the SIP status. `SipCallError.from(response)` returns `null` if it isn't a SIP failure:

```java
import io.livekit.server.SipCallError;

Response<LivekitSip.SIPParticipantInfo> response =
    api.getSip().createSipParticipant("ST_trunk", "+15105550100", "my-room", null).execute();
if (!response.isSuccessful()) {
    SipCallError error = SipCallError.from(response);
    if (error != null && Integer.valueOf(486).equals(error.getSipStatusCode())) {
        // callee is busy
    }
}
```

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
