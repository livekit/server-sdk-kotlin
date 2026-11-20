---
"server-sdk-kotlin": patch
---

WebhookReceiver accepts an optional `leewaySeconds` constructor parameter to tolerate clock skew when verifying webhook tokens. Defaults to 0, so existing behavior is unchanged.
