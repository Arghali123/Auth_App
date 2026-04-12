```
security:
  jwt:
    secret: ${JWT_SECRET:saiofal;vkasfbehwqpofhjashofjaswfpojasfvbafop[hfifonfo[shjfwefjjfslajfsahfoshffasnbash[f}
    issuer: ${JWT_ISSUER:api.substring.com}
    access-ttl-seconds: ${JWT_ACCESS_TTL_SECONDS:600}
    refresh-ttl-seconds: ${JWT_REFRESH_TTL_SECONDS:86400}
    refresh-token-cookie-name: ${JWT_REFRESH_COOKIE_NAME:refreshToken}
    cookie-secure: ${JWT_COOKIE_SECURE:true}
    cookie-http-only: ${JWT_COOKIE_HTTP_ONLY:true}
    cookie-same-site: ${JWT_COOKIE_SAME_SITE:lax}
    cookie-domain: ${JWT_COOKIE_DOMAIN:localhost}
```

The syntax `${VARIABLE:DEFAULT_VALUE}` is a placeholder: it looks for an environment variable first, and if it doesn't find one, it falls back to the hardcoded string after the colon.

---

### ## 1. Token Identity & Security
This section defines what the token is and how it’s signed.

* **`secret`**: The "password" used to sign the tokens.
  > **Note:** The long string provided is a fallback. In production, you should *always* provide a unique, high-entropy key via the `JWT_SECRET` environment variable to prevent attackers from forging tokens.
* **`issuer`**: A string identifying who created the token (in this case, `api.substring.com`). This is checked during validation to ensure the token came from a trusted source.

### ## 2. Token Lifespan (TTL)
TTL stands for "Time To Live." It dictates how long a user stays logged in.

| Key | Default Value | Description |
| :--- | :--- | :--- |
| **`access-ttl-seconds`** | 600 (10 mins) | Short-lived. Used for every API request. If stolen, it expires quickly. |
| **`refresh-ttl-seconds`** | 86400 (24 hours) | Long-lived. Used to request a *new* access token without making the user log in again. |



---

### ## 3. Cookie Security Settings
The configuration suggests that the **Refresh Token** is stored in a browser cookie rather than LocalStorage. This is a best practice for preventing **XSS (Cross-Site Scripting)** attacks.

* **`refresh-token-cookie-name`**: The name the browser will show for this cookie (default: `refreshToken`).
* **`cookie-secure: true`**: The cookie will **only** be sent over encrypted HTTPS connections (prevents "man-in-the-middle" sniffing).
* **`cookie-http-only: true`**: This is critical. It prevents JavaScript from accessing the cookie. If a hacker runs a malicious script on your site, they cannot "steal" the refresh token.
* **`cookie-same-site: lax`**: Protects against **CSRF (Cross-Site Request Forgery)**. "Lax" means the cookie is sent when navigating to the site but restricted on cross-site sub-requests (like images or frames).
* **`cookie-domain`**: Defines which domain the cookie is valid for. It's set to `localhost` for development, but in production, this would be `substring.com`.

---

### ### Summary for Learning
To understand this efficiently, think of it as two layers:
1.  **The Token Layer:** How long does the "key" work (`ttl`) and who signed it (`secret/issuer`)?
2.  **The Transport Layer:** How do we move that "key" safely to the browser? (The `cookie` settings ensure the key isn't stolen by scripts or sent over unencrypted wires).

