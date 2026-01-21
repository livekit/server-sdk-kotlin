---
"server-sdk-kotlin": minor
---

Add critical bug fixes from v10 into v0.9.0

- Support for Arrays in RoomConfiguration (https://github.com/livekit/server-sdk-kotlin/pull/121)
- Stop setting redundant jti claim (https://github.com/livekit/server-sdk-kotlin/pull/124)

v10 introduced several bug fixes from v0.9.0 that are not related to Protobuff.
It also introduced a hard dependency on Protobuff Version 4.

Many enterprises remain on Protobuff Version 3 due to a migration to Version 4 being complex and expensive.

Not being able to set any array related field on AccessToken/RoomConfiguration is the most critical and prevents implementation of recommended integration patterns with AccessTokens and Rooms.

An additional minor upgrade to v0.9.1 to address these will help improve adoption of the library
