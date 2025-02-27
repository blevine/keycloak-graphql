package net.brianlevine.keycloak.graphql.mutations;

import graphql.GraphQLContext;
import graphql.GraphQLException;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLRootContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import net.brianlevine.keycloak.graphql.ErrorCode;
import net.brianlevine.keycloak.graphql.ExceptionWithCode;
import net.brianlevine.keycloak.graphql.types.UserType;
import net.brianlevine.keycloak.graphql.util.Auth;
import net.brianlevine.keycloak.graphql.util.Util;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.UsersResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import java.net.URI;


import static net.brianlevine.keycloak.graphql.Constants.KEYCLOAK_SESSION_KEY;

public class UserMutation {

    @GraphQLMutation
    public UserType createUser(UserType user, @GraphQLRootContext GraphQLContext ctx) {

        // TODO: Trying out using the *Resource classes rather than cloning the code. This makes
        //       error handling slightly more challenging.
        KeycloakSession session = ctx.get(KEYCLOAK_SESSION_KEY);
        RealmModel realm = session.getContext().getRealm();
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, ctx);
        AdminPermissionEvaluator eval = AdminPermissions.evaluator(session, realm, auth);
        AdminEventBuilder eventBuilder = Util.createAdminEventBuilder(session, realm, auth);
        UsersResource usersResource = new UsersResource(session, eval, eventBuilder);

        UserType ret = null;
        try {
            Response res = usersResource.createUser(user.getUserRepresentation());

            // Get the ID from the Location header
            URI location = res.getLocation();
            String[] components = location.getPath().split("/");
            String id = components[components.length - 1];

            // Get back the user we just created and convert to a UserType
            UserModel userModel = session.users().getUserById(realm, id);
            ret =  new UserType(session, realm, userModel);
        } catch (Exception e) {
            handleError(e);
        }

        return ret;
    }

    // TODO: Will probably need to refactor into generic utility(ies) for other mutations
    private static void handleError(Exception e) {
        ExceptionWithCode ee;

        if (e instanceof ForbiddenException) {
            ee = new ExceptionWithCode("Could not create user: Forbidden", e, ErrorCode.Forbidden);
        }
        else if (e instanceof ErrorResponseException) {
            Response r = ((ErrorResponseException)e).getResponse();
            Object entity = r.getEntity();

            String errorMessage = "";
            String errorID = "";
            if (entity instanceof ErrorRepresentation) {
                errorMessage = ((ErrorRepresentation) entity).getErrorMessage();
            } else if (entity instanceof OAuth2ErrorRepresentation) {
                errorMessage = ((OAuth2ErrorRepresentation) entity).getErrorDescription();
                errorID = ((OAuth2ErrorRepresentation) entity).getError();
            }

            ee = new ExceptionWithCode("Could not create user: " + errorMessage, e);
            int statusCode = r.getStatus();

            ErrorCode errorCode = ErrorCode.Unknown;
            switch (statusCode) {
                case 400:
                    if (!errorID.isEmpty()) {
                        if (errorID.contains("invalidPassword")) {
                            errorCode = ErrorCode.InvalidPassword;
                        }
                    }
                    break;
                case 409:
                    errorCode = ErrorCode.DuplicateUser;
                    break;
            }

            ee.setCode(errorCode);
        }
        else {
            ee = new ExceptionWithCode("Could not create user: " + e.getMessage(), e, ErrorCode.Unknown);
        }

        throw ee;
    }
}
