package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import io.leangen.graphql.execution.SortField;
import net.brianlevine.keycloak.graphql.annotations.GraphQLOverrideTypeName;

@GraphQLOverrideTypeName
@GraphQLType(name = "PagingOptions")
public class PagingOptions {
    public static final PagingOptions DEFAULT = new PagingOptions();

    @GraphQLInputField(defaultValue="0")
    public int start = 0;

    @GraphQLInputField(defaultValue="100")
    public int limit = 100;

    @GraphQLInputField(defaultValue="[]", description="** Not yet supported **")
    public SortField[] sort = null;


}

