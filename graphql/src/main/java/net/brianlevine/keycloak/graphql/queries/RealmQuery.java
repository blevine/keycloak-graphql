package net.brianlevine.keycloak.graphql.queries;

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
import java.util.stream.Stream;

public class RealmQuery {

    @GraphQLQuery(name = "realms")
    public Page<RealmType> getRealms(
            @GraphQLRootContext GraphQLContext ctx,
            @GraphQLArgument(name="limit", defaultValue = "100") int limit,
            @GraphQLArgument(name="start", defaultValue = "0") int start) {

        KeycloakSession session = ctx.get("keycloak.session");
        Stream<RealmModel> realms = session.realms().getRealmsStream();
        HttpHeaders headers = ctx.get("headers");
        AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);

        // TODO: If we use ExportUtils.exportRealm() we could get more realm information into the realm
        //       representation. If we do that, we might want to look at the GraphQL query to set the
        //       ExportOptions. OR, maybe we just want to have our own version of exportRealm() with even finer-
        //       grained options based on what fields were requested in the GraphQL query
        List<RealmType> realmTypes = realms.map(m -> {
            RealmRepresentation rep = toRealmRep(session, auth, m);
            return rep != null ? new RealmType(session, rep) : null;
        }).filter(Objects::nonNull).toList();
        Page<RealmType> page = new Page<>(realmTypes.size(), limit, realmTypes);
        return page;
    }

    @GraphQLQuery(name = "realm")
    public RealmType getRealmById(@GraphQLArgument String id, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get("keycloak.session");
        RealmModel realm = session.realms().getRealm(id);

        return toRealmType(ctx, session, realm);
    }

    @GraphQLQuery(name = "realm")
    public RealmType getRealmByName(@GraphQLArgument String name, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get("keycloak.session");
        RealmModel realm = session.realms().getRealmByName(name);

        return toRealmType(ctx, session, realm);
    }



    @GraphQLQuery(name = "realm")
    public RealmType getRealm( @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = ctx.get("keycloak.session");
        RealmModel realm = session.getContext().getRealm();

        return toRealmType(ctx, session, realm);
    }

    private RealmType toRealmType(@GraphQLRootContext GraphQLContext ctx, KeycloakSession session, RealmModel realm) {
        if (realm != null) {
            HttpHeaders headers = ctx.get("headers");
            AdminAuth auth = Auth.authenticateRealmAdminRequest(session, headers);
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
            return ModelToRepresentation.toRepresentation(session, realm, false);
        } else if (AdminPermissions.realms(session, auth).isAdmin(realm)) {
            RealmRepresentation rep = new RealmRepresentation();
            rep.setRealm(realm.getName());
            return rep;
        }

        return null;
    }
}
