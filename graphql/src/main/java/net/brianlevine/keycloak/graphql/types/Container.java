package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.types.GraphQLInterface;


@GraphQLInterface(
        name = "Container",
        implementationAutoDiscovery = true,
        scanPackages = "net.brianlevine.keycloak.graphql.types",
        description = "A role's container: RealmType or ClientType"
)
public interface Container {
    String getId();
    String getName();
}