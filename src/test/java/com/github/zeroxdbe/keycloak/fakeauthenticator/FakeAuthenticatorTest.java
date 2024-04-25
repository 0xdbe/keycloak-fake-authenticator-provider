package com.github.zeroxdbe.keycloak.fakeauthenticator;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FakeAuthenticatorTest {

    // Shared between all tests in this class.
    static Playwright playwright;
    static Browser browser;
    static KeycloakServer keycloakServer;

    static int debug;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @BeforeAll
    static void startKeycloak() {
        keycloakServer = new KeycloakServer();
        keycloakServer.createUser("John", "Doe", "john@acme.com", "test");
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @AfterAll
    static void stopKeycloak() {
        keycloakServer.stop();
    }

    @BeforeEach
    void createContextAndPage() {
        debug = 1;
        context = browser.newContext();
        context.tracing().start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true)
        .setSources(true));
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    public void whenCallingSayHello_thenReturnHello() {
        String Greeting = "Hello";
        assertTrue("Hello".equals(Greeting));
    }

    @Test
    public void shouldBeAvailable() {
        Response response = page.navigate(keycloakServer.getAuthorizationUrl("playwright"));
        Assert.assertEquals(200, response.status());
        context.tracing().stop(new Tracing.StopOptions()
        .setPath(Paths.get("target/playwright/trace/shouldBeAvailable.zip")));
    }

}
