package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.types.GraphQLInterface;


@GraphQLInterface(name = "Container", implementationAutoDiscovery = true, scanPackages = "net.brianlevine.keycloak.graphql.types")
public interface Container {
    String getId();
    String getName();
}