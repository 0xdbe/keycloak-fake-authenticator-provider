package com.github.zeroxdbe.keycloak.fakeauthenticator;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public class KeycloakServer {

    @Container 
    private KeycloakContainer keycloakServer;

    public KeycloakServer() {
        keycloakServer = new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
            .withProviderClassesFrom("target/classes")
            .withRealmImportFile("/realm-test.json")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/"));
        keycloakServer.start();
    }

    public void createUser(String firstname, String lastname, String email, String password) {
        Keycloak cli = this.createKeycloakClient();

        // Define user
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEmail(email);
        user.setEnabled(true);

        RealmResource realmResource = cli.realm("demo");
        UsersResource usersResource = realmResource.users();

        // Create user (requires manage-users role)
        Response response = usersResource.create(user);
        String userId = CreatedResponseUtil.getCreatedId(response);

        // Define password credential
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        // Set password credential
        UserResource userResource = usersResource.get(userId);
        userResource.resetPassword(passwordCred);

        cli.close();
    }

    public String getAuthorizationUrl(String clientId) {
        String host = keycloakServer.getAuthServerUrl();
        String redirectURI=encodeValue("http://playwright.local/callback");
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        String challenge = UUID.randomUUID().toString();
        MessageDigest digest;
        String encodedChallenge = "unencoded-challenge";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            encodedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(challenge.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String url = MessageFormat.format(
            "{0}/realms/demo/protocol/openid-connect/auth?client_id={1}&redirect_uri={2}&state={3}&response_mode=fragment&response_type=code&scope=openid&nonce={4}&code_challenge={5}&code_challenge_method=S256", 
            host, clientId, redirectURI, state, nonce, encodedChallenge);
        return url;
    }

    public void stop() {
        keycloakServer.stop();
    }

    private Keycloak createKeycloakClient() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakServer.getAuthServerUrl())
            .realm("master")
            .clientId("admin-cli")
            .username(keycloakServer.getAdminUsername())
            .password(keycloakServer.getAdminPassword())
            .build();
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
