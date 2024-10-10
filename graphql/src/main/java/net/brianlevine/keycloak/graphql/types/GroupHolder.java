package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.*;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import graphql.GraphQLContext;

import java.util.List;
import java.util.stream.Stream;

public interface GroupHolder extends BaseType {
    @GraphQLQuery
    default Page<GroupType> getGroups(PagingOptions options, @GraphQLRootContext GraphQLContext ctx) {
        options = options == null ? new PagingOptions() : options;
        long totalCount = getGroupsCount(ctx);
        Stream<GroupModel> groupModels = getGroupsStream(options.start, options.limit, ctx);

        KeycloakSession kcSession = getKeycloakSession();
        RealmModel realmModel = getRealmModel();
        List<GroupType> groups = groupModels.map(gm -> new GroupType(kcSession, realmModel, gm)).toList();

        return new Page<>((int) totalCount, options.limit, groups);
    }

    // Note: Implementations should make these as @GraphQLIgnore
    Stream<GroupModel> getGroupsStream(GraphQLContext ctx);
    Stream<GroupModel> getGroupsStream(int start, int limit, GraphQLContext ctx);
    default long getGroupsCount(GraphQLContext ctx) {
        return getGroupsStream(ctx).count();
    }

}
