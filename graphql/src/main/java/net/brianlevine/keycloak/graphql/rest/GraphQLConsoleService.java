package net.brianlevine.keycloak.graphql.rest;

import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.AbstractSecuredLocalService;

import java.net.URI;
import java.util.Set;

public class GraphQLConsoleService extends AbstractSecuredLocalService {
    public GraphQLConsoleService(KeycloakSession session, ClientModel client) {
        super(session, client);
    }

    public static GraphQLConsoleService getGraphQLConsoleService(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();

        //TODO: We should probably define our own client.
        ClientModel client = session.clients().getClientByClientId(realm, Constants.ADMIN_CONSOLE_CLIENT_ID);
        return new GraphQLConsoleService(session, client);
    }

    @Override
    protected Set<String> getValidPaths() {
        return Set.of();
    }

    @Override
    protected URI getBaseRedirectUri() {
        return null;
    }
}
