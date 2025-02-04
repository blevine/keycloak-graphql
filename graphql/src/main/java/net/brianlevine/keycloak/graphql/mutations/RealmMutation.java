package net.brianlevine.keycloak.graphql.mutations;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLRootContext;
import jakarta.ws.rs.NotAuthorizedException;
import net.brianlevine.keycloak.graphql.types.RealmType;
import net.brianlevine.keycloak.graphql.util.Auth;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import static net.brianlevine.keycloak.graphql.Constants.KEYCLOAK_SESSION_KEY;

public class RealmMutation {
    @GraphQLMutation
    public RealmType createRealm(RealmType realm, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get(KEYCLOAK_SESSION_KEY);
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, ctx);

        if (AdminPermissions.realms(session, auth).canCreateRealm()) {
            RealmManager realmMgr = new RealmManager(session);
            RealmModel realmModel = realmMgr.importRealm(realm.getRealmRepresentation(), false);
            RealmType realmType = new RealmType(session, realmModel);
            return realmType;
        }

        throw new NotAuthorizedException("Caller does not have the required permission.");
    }
}
