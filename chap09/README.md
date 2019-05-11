## chap09

### build.gradle spring-security-oauth2-client 추가

```
compile('org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.0.0.RELEASE')
```


#### Application.yml 추가
```
server:
  ssl:
    key-alias: primavera
    key-store: chap09/primavera.p12
    key-store-type: PKCS12
    key-store-password: primavera
    enabled: true
  port: 8443

google:
  client:
    clientId: ${OAUTH2_GOOGLE_CLIENTID}
    clientSecret: ${OAUTH2_GOOGLE_CLIENTSECRET}
    accessTokenUri: https://www.googleapis.com/oauth2/v4/token
    userAuthorizationUri: https://accounts.google.com/o/oauth2/v2/auth
    clientAuthenticationScheme: form
    scope:
      - email
      - profile
  resource:
    userInfoUri: https://www.googleapis.com/oauth2/v3/userinfo

facebook:
  client:
    clientId: ${OAUTH2_FACEBOOK_CLIENTID}
    clientSecret: ${OAUTH2_FACEBOOK_CLIENTSECRET}
    accessTokenUri: https://graph.facebook.com/oauth/access_token
    userAuthorizationUri: https://www.facebook.com/dialog/oauth
    tokenName: oauth_token
    authenticationScheme: query
    clientAuthenticationScheme: form
  resource:
    userInfoUri: https://graph.facebook.com/me

github:
  client:
    clientId: ${OAUTH2_GITHUB_CLIENTID}
    clientSecret: ${OAUTH2_GITHUB_CLIENTSECRET}
    accessTokenUri: https://github.com/login/oauth/access_token
    userAuthorizationUri: https://github.com/login/oauth/authorize
    clientAuthenticationScheme: form
  resource:
    userInfoUri: https://api.github.com/user
```

### Application SSL

#### Key Generation
```
keytool -genkeypair -alias primavera -storetype PKCS12 -keyalg RSA -keysize 2048  -keystore primavera.p12 -validity 3650
```

#### Local Damian
* host 파일 수정
```
127.0.0.1       local.primavera.com
```

#### Social Authorization callback URL
 * https://localhost:8443/login/google
 * https://localhost:8443/login/facebook
 * https://localhost:8443/login/github

### ETC
* https://console.cloud.google.com
* https://developers.facebook.com
* https://github.com