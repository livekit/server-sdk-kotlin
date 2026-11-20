---
"server-sdk-kotlin": patch
---

WebhookReceiver accepts an optional `leewaySeconds` constructor parameter to tolerate clock skew when verifying webhook tokens. The default changes from 0 to 60 seconds to align with the other LiveKit server SDKs. Pass `leewaySeconds = 0` to keep the previous strict behavior.
