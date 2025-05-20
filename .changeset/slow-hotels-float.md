---
"server-sdk-kotlin": patch
---

### Kotlin
- **Update to Kotlin 1.9.25**  
  Upgraded to the latest patch version for better stability and compatibility with newer tools.  
  _Reference_: [Kotlin 1.9.25 Release Notes](https://kotlinlang.org/docs/whatsnew1920.html)

---

### Protobuf
- **Update `protobufVersion` to 4.29.4**  
  Ensures compatibility with the MySQL JDBC connector.  
  _Note_: Previous versions had compatibility issues when working with certain database drivers.

---

### Retrofit
- **Update `com.squareup.retrofit2:retrofit` to 2.11.0**
- **Update `com.squareup.retrofit2:converter-protobuf` to 2.11.0**  
  Fixes vulnerabilities reported in older versions of Retrofit.  
  _Reference_: [Retrofit 2.11.0 Changelog](https://github.com/square/retrofit/blob/master/CHANGELOG.md)  
  _Security Advisory_:
        - [CVE-2023-2976](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2023-2976)
        - [CVE-2022-24329](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2022-24329)
        - [CVE-2020-8908](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2020-8908)
        - [CVE-2020-29582](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2020-29582)
        - [CVE-2020-15250](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2020-15250)

---

### Auth0 Java JWT
- **Update `com.auth0:java-jwt` to 4.5.0**  
  Addresses security issues in earlier releases and includes bug fixes.  
  _Reference_: [java-jwt GitHub Releases](https://github.com/auth0/java-jwt/releases)

---
