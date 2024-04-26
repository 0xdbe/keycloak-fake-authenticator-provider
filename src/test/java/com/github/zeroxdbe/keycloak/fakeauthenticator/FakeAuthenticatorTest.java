package com.github.zeroxdbe.keycloak.fakeauthenticator;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FakeAuthenticatorTest {

    // Shared between all tests in this class.
    static Playwright playwright;
    static Browser browser;
    static KeycloakServer keycloakServer;

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
    void shouldBeAvailable() {
        Response response = page.navigate(keycloakServer.getAuthorizationUrl("playwright"));
        Assert.assertEquals(200, response.status());
        context.tracing().stop(new Tracing.StopOptions()
        .setPath(Paths.get("target/playwright/trace/shouldBeAvailable.zip")));
    }

    @Test
    void shouldFailOTP() {
      page.navigate(keycloakServer.getAuthorizationUrl("playwright"));
      page.getByLabel("Email").fill("john@acme.com");
      page.querySelector("#password").fill("test");
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
      page.screenshot(new Page.ScreenshotOptions()
        .setPath(Paths.get("target/playwright/screenshot/shouldFailOTP.png")));
      assertThat(page.getByLabel("One-time code")).isVisible();
      page.getByLabel("One-time code").fill("0000");
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
      assertThat(page.getByText("Invalid username or password.")).isVisible();
      page.screenshot(new Page.ScreenshotOptions()
        .setPath(Paths.get("target/playwright/screenshot/shouldFailOTP.png")));
      context.tracing().stop(new Tracing.StopOptions()
      .setPath(Paths.get("target/playwright/trace/shouldFailOTP.zip")));
    }

    @Test
    void shouldPassOTP() {
      page.navigate(keycloakServer.getAuthorizationUrl("playwright"));
      page.getByLabel("Email").fill("john@acme.com");
      page.querySelector("#password").fill("test");
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
      page.screenshot(new Page.ScreenshotOptions()
        .setPath(Paths.get("target/playwright/screenshot/shouldPassOTP_1.png")));
      assertThat(page.getByLabel("One-time code")).isVisible();
      page.getByLabel("One-time code").fill("1234");
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")).click();
      assertThat(page.getByText("Invalid username or password.")).not().isVisible();
      page.screenshot(new Page.ScreenshotOptions()
        .setPath(Paths.get("target/playwright/screenshot/shouldPassOTP_2.png")));
      context.tracing().stop(new Tracing.StopOptions()
      .setPath(Paths.get("target/playwright/trace/shouldPassOTP.zip")));
    }

}
