## chap09

### build.gradle spring-security-oauth2-client 추가

```
compile('org.springframework.security:spring-security-oauth2-client:5.1.5.RELEASE')
```

### HTTPS

#### Key Generation
```
 keytool -genkey -alias primavera -keyalg RSA -sigalg MD5withRSA -keystore primavera.jks -storepass primavera  -keypass primavera -validity 9999  -keystore primavera.jks
keytool -list -keystore primavera.jks
```

#### Application.yml 추가
```
server:
  ssl:
    key-alias: primavera
    key-password: primavera
    key-store: classpath:primavera.jks
    key-store-provider: SUN
    key-store-type: JKS
  port: 8443
```

* https://developers.google.com/identity/sign-in/web/


keytool -genkeypair -alias primavera -keyalg RSA -validity 7 -storepass primavera -keystore keystore
keytool -list -v -keystore keystore
keytool -export -alias primavera -keystore keystore -rfc -file primavera.cer
keytool -import -alias primavera -file primavera.cer -keystore truststore
keytool -list -v -keystore truststore
keytool -import -file primavera.cer -alias primavera