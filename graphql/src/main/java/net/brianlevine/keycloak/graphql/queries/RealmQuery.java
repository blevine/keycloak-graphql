package net.brianlevine.keycloak.graphql.queries;

import jakarta.ws.rs.ForbiddenException;
import net.brianlevine.keycloak.graphql.util.Page;
import net.brianlevine.keycloak.graphql.util.Auth;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;

import net.brianlevine.keycloak.graphql.types.RealmType;
import jakarta.ws.rs.core.HttpHeaders;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;


import java.util.List;
import java.util.Objects;

public class RealmQuery {

    @GraphQLQuery(name = "realms", description = "Return a collection of realms that are viewable by the caller.")
    public Page<RealmType> getRealms(
            @GraphQLRootContext GraphQLContext ctx,
            @GraphQLArgument(name="limit", defaultValue = "100") int limit,
            @GraphQLArgument(name="start", defaultValue = "0") int start) {

        KeycloakSession session = ctx.get("keycloak.session");
        HttpHeaders headers = ctx.get("headers");
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);
        Page<RealmType> ret;

        try {
            List<RealmRepresentation> realms = session.realms().getRealmsStream()
                    .map(realm -> toRealmRep(session, auth, realm))
                    .filter(Objects::nonNull)
                    .toList();

            List<RealmType> realmTypes = realms.stream()
                    .map(rep -> rep != null ? new RealmType(session, rep) : null)
                    .toList();

            ret = new Page<>(realms.size(), limit, realmTypes);
        } catch (ForbiddenException e) {
            ret = Page.emptyPage();
        }

        return ret;
    }

    @GraphQLQuery(name = "realm", description = "Get a realm by ID.")
    public RealmType getRealmById(@GraphQLArgument String id, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get("keycloak.session");
        RealmModel realm = session.realms().getRealm(id);
        HttpHeaders headers = ctx.get("headers");
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);

        return toRealmType(session, realm, auth);
    }

    @GraphQLQuery(name = "realm", description = "Get realm by name.")
    public RealmType getRealmByName(@GraphQLArgument String name, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get("keycloak.session");
        RealmModel realm = session.realms().getRealmByName(name);
        HttpHeaders headers = ctx.get("headers");
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);
        return toRealmType(session, realm, auth);
    }



    @GraphQLQuery(name = "realm", description = "Get the current realm (i.e., the realm against which the caller authenticated.")
    public RealmType getRealm( @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get("keycloak.session");
        RealmModel realm = session.getContext().getRealm();
        HttpHeaders headers = ctx.get("headers");
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);

        return toRealmType(session, realm, auth);
    }

    private RealmType toRealmType(KeycloakSession session, RealmModel realm, AdminAuth auth) {
        if (realm != null) {
            RealmRepresentation rep = toRealmRep(session, auth, realm);
            return rep != null ? new RealmType(session, rep) : null;
        }

        return null;
    }

    /**
     * From RealmsAdminResource
     */
    protected RealmRepresentation toRealmRep(KeycloakSession session, AdminAuth auth, RealmModel realm) {
        if (AdminPermissions.realms(session, auth).canView(realm)) {
            // TODO: Choice of adding additional fields to RealmRepresentation by setting export and internal
            //       arguments. Setting both to true gets us the most fields. Is this OK?
            final boolean internal = true;
            final boolean export = true;
            return ModelToRepresentation.toRepresentation(session, realm, internal, export);
        } else if (AdminPermissions.realms(session, auth).isAdmin(realm)) {
            RealmRepresentation rep = new RealmRepresentation();
            rep.setRealm(realm.getName());
            return rep;
        }

        return null;
    }
}
