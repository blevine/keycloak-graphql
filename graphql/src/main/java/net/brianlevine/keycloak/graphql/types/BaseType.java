package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLIgnore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public interface BaseType {

    // Note: Implementations should make these as @GraphQLIgnore
    RealmModel getRealmModel();
    KeycloakSession getKeycloakSession();
}