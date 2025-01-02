package net.brianlevine.keycloak.graphql;

public interface Constants {
    static final int TOKEN_EXPIRATION_SAFETY = 15 * 1000; // in millis
    static final String ACCESS_TOKEN_KEY = "accessToken";
    static final String ACCESS_TOKEN_EXPIRES_KEY = "accessTokenExpires";
    static final String REALM_NAME_KEY = "realm";
    static final String SESSION_ID_KEY = "sessionId";
    static final String HTTP_HEADERS_KEY = "headers";
    static final String KEYCLOAK_SESSION_KEY = "keycloakSession";
    static final String IS_SUBSCRIPTION_KEY = "isSubscription";
}
