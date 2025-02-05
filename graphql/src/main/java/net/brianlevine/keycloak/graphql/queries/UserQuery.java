package net.brianlevine.keycloak.graphql.queries;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import net.brianlevine.keycloak.graphql.types.UserType;
import net.brianlevine.keycloak.graphql.util.Auth;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.AdminAuth;

import static net.brianlevine.keycloak.graphql.Constants.KEYCLOAK_SESSION_KEY;


public class UserQuery {
    @GraphQLQuery(name = "currentUser", description = "Returns the (current) authenticated user.")
    public UserType getCurrentUser(@GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get(KEYCLOAK_SESSION_KEY);
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, ctx);
        UserModel user = auth.getUser();

        return new UserType(session, session.getContext().getRealm(), user);
    }
}
