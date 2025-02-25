package net.brianlevine.keycloak.graphql.mutations;

import io.restassured.response.ValidatableResponse;
import net.brianlevine.keycloak.graphql.KeycloakGraphQLTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class RealmMutationTest extends KeycloakGraphQLTest {
    @Test
    public void shouldCreateRealmMutation() {
        String realmName = "testrealm";

        // language=GraphQL
        String mutation = String.format("""
                mutation {
                   createRealm(realm: {
                     name: "%s"
                   }) {
                     id
                     name
                   }
                 }
            """, realmName);

        ValidatableResponse response = sendGraphQLRequestAsMasterAdmin(mutation);
        response.body("data.createRealm.name", equalTo(realmName));
        response.body("data.createRealm.id", notNullValue());
        //response.log().all();

    }

    @Test
    public void shoulReturnForbiddenWhenNotMasterRealm() {
        String realmName = "testrealm";

        // language=GraphQL
        String mutation = String.format("""
                mutation {
                   createRealm(realm: {
                     name: "%s"
                   }) {
                     id
                     name
                   }
                 }
            """, realmName);

        // User is admin, but not in the master realm
        ValidatableResponse response = sendGraphQLRequestAsTestAdmin(mutation);
        response.body("errors[0].extensions.code", equalTo("ForbiddenException"));
        response.body("data.createRealm", nullValue());
        //response.log().all();
    }

    @Test
    public void shoulReturnForbiddenWhenUserNotAdmin() {
        String realmName = "testrealm";

        // language=GraphQL
        String mutation = String.format("""
                mutation {
                   createRealm(realm: {
                     name: "%s"
                   }) {
                     id
                     name
                   }
                 }
            """, realmName);


        createUser("notadmin", "notadmin");
        ValidatableResponse response = sendGraphQLRequestAsUser(mutation, TEST_REALM, "notadmin", "notadmin");
        //response.log().all();
        response.body("errors[0].extensions.code", equalTo("ForbiddenException"));
        response.body("data.createRealm", nullValue());

    }
}
