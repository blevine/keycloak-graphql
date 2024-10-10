package net.brianlevine.keycloak.graphql.types;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;
import java.util.stream.Stream;

public interface RoleHolder extends BaseType {

    @GraphQLQuery
    default Page<RoleType> getRoles(PagingOptions options, @GraphQLRootContext GraphQLContext ctx) {

        long count = getRolesCount(ctx);
        Stream<RoleModel> roles = getRolesStream(options.start, options.limit, ctx);


        List<RoleType> roleTypes = roles.map(r -> {
            RoleRepresentation roleRep = ModelToRepresentation.toRepresentation(r);
            return new RoleType(getKeycloakSession(), getRealmModel(), roleRep);
        }).toList();

        return new Page<>((int) count, options.limit, roleTypes);
    }

    // Note: Implementations should make these as @GraphQLIgnore
    Stream<RoleModel> getRolesStream(GraphQLContext ctx);
    Stream<RoleModel> getRolesStream(int start, int limit, GraphQLContext ctx);
    default long getRolesCount(GraphQLContext ctx) {
        return getRolesStream(ctx).count();
    }
}
