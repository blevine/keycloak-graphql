package net.brianlevine.keycloak.graphql.queries;

import net.brianlevine.keycloak.graphql.util.Page;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;

import net.brianlevine.keycloak.graphql.types.RealmType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;

import java.util.List;
import java.util.stream.Stream;

public class RealmQuery {

    @GraphQLQuery(name = "realms")
    public Page<RealmType> getRealms(
            @GraphQLRootContext GraphQLContext ctx,
            @GraphQLArgument(name="count", defaultValue = "10") int count,
            @GraphQLArgument(name="offset", defaultValue = "0") int offset) {
        KeycloakSession session = ctx.get("keycloak.session");
        Stream<RealmModel> realms = session.realms().getRealmsStream();

        // TODO: If we use ExportUtils.exportRealm() we could get more realm information into the realm
        //       representation. If we do that, we might want to look at the GraphQL query to set the
        //       ExportOptions. OR, maybe we just want to have our own version of exportRealm() with even finer-
        //       grained options based on what fields were requested in the GraphQL query
        List<RealmType> realmTypes = realms.map(m -> new RealmType(ModelToRepresentation.toRepresentation(session, m, true))).toList();
        Page<RealmType> page = new Page<>(realmTypes.size(), 1, realmTypes);
        return page;
    }
}
