package net.brianlevine.keycloak.graphql.util;

import graphql.GraphQLContext;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import static net.brianlevine.keycloak.graphql.Constants.*;
import static org.keycloak.TokenVerifier.IS_ACTIVE;

public class Auth {
    /**
     * From AdminRoot
     */

    public static AdminAuth authenticateRealmAdminRequest(KeycloakSession session, GraphQLContext ctx) {
        return authenticateRealmAdminRequest(session, (HttpHeaders)ctx.get(HTTP_HEADERS_KEY));
    }

    private static AdminAuth authenticateRealmAdminRequest(KeycloakSession session, HttpHeaders headers) {
        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }
        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        if (realm == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }
        session.getContext().setRealm(realm);

        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                //BJL.setConnection(session.getContext().getConnection())
                .setHeaders(headers)
                .authenticate();

        if (authResult == null) {
            //Logger.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        return new AdminAuth(realm, authResult.getToken(), authResult.getUser(), authResult.getClient());
    }

    public static AdminPermissionEvaluator getAdminPermissionEvaluator(GraphQLContext ctx, RealmModel realm) {
        return getAdminPermissionEvaluator(ctx.get(HTTP_HEADERS_KEY), ctx.get(KEYCLOAK_SESSION_KEY), realm);

    }

    public static AdminPermissionEvaluator getAdminPermissionEvaluator(HttpHeaders headers, KeycloakSession session, RealmModel realm) {
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);
        AdminPermissionEvaluator evaluator = AdminPermissions.evaluator(session, realm, auth);
        return evaluator;
    }

    public static AccessToken verifyAccessToken(String token, @Nullable KeycloakSession session) {

        try {
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(token, AccessToken.class).withChecks(IS_ACTIVE);

            if (session != null) {
                //RealmModel realm = session.getContext().getRealm();

                SignatureVerifierContext verifierContext = session.getProvider(SignatureProvider.class, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
                verifier.verifierContext(verifierContext);

            }

            AccessToken accessToken = verifier.verify().getToken();

            return accessToken;
        } catch (VerificationException e) {
            return null;
        }
    }
}
