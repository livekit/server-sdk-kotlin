---
"server-sdk-kotlin": patch
---

WebhookReceiver requires the JWT exp claim. java-jwt accepts a signed token with no expiry unless presence is required.
