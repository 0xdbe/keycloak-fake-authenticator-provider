package com.github.zeroxdbe.keycloak.fakeauthenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;


public final class FakeAuthenticatorForm extends AbstractUsernameFormAuthenticator {

     @Override
    public void authenticate(AuthenticationFlowContext context) {
        try {
            Response challengeResponse = challenge(context, null);
            context.challenge(challengeResponse);
        } catch (Exception e) {
            ServicesLogger.LOGGER.error("Code Authenticator provider e", e);
            context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (validateOTPCode(context, context.getUser(), formData)) {
            context.success();
            return;
        }
        context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);    
    }

    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginTotp();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // Authentication by Code is configured for all users 
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        throw new UnsupportedOperationException("Unimplemented method 'setRequiredActions'");
    }

    public boolean validateOTPCode(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {
        String OTPCode = inputData.getFirst("otp");
        ServicesLogger.LOGGER.infof("Fake OTP code: %s", OTPCode);
        if (OTPCode != null && !OTPCode.isEmpty()) {
            if (OTPCode.equals("1234")) {
                return true;
            }
        }
        return false;
    }

}