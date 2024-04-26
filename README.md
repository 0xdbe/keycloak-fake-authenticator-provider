# keycloak Fake Authenticator

This provider is a fake OTP authenticator


## Build

```
mvn clean package
```

## Deploy

- Copy provider

```
sudo cp target/keycloak-fake-authenticator-provider-1.0.0.jar $KC_HOME/providers
```

- Add `Fake OTP Form` step in your authentication flow


## Debug

- Run keycloak

```
sudo kc.sh --debug --verbose start-dev
```

- Add break point in source code

- Run `Debug (Attach)` debug config

# Test

- run test on your host

```
PWDEBUG=1 PLAYWRIGHT_JAVA_SRC="src/test/java" mvn test
```