package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.annotations.GraphQLOverrideTypeName;


@GraphQLOverrideTypeName
@GraphQLType(name = "PagingConfig", description = "Standard arguments for controlling the paging behavior of collections.")
public class StdCollectionArgs {
    @GraphQLInputField(defaultValue="0")
    public int start = 0;

    @GraphQLInputField(defaultValue="100")
    public int limit = 100;

    @GraphQLInputField(defaultValue="null")
    public String[] sort = null;
}
