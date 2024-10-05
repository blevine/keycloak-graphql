package net.brianlevine.keycloak.graphql.queries;

import io.restassured.response.ValidatableResponse;
import net.brianlevine.keycloak.graphql.KeycloakGraphQLTest;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UserResource;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class RealmQueriesTest extends KeycloakGraphQLTest {

    @Test
    public void shouldReturnTheDefaultRealm() {

        // language=GraphQL
        String query = """
                query {
                    realm {
                        name
                    }
                }
                """;

        sendGraphQLRequestAsTestAdmin(query).body("data.realm.name", equalTo(TEST_REALM));
    }

    @Test
    public void shouldReturnOwnRealm() {
        // language=GraphQL
        String query = """
                query {
                    realms {
                        items {
                            name
                        }
                    }
                }
                """;

        sendGraphQLRequestAsTestAdmin(query)
                .body("data.realms.items.name", hasSize(1))
                .and()
                .body("data.realms.items.name[0]", equalTo(TEST_REALM));
    }

    @Test
    public void shouldReturnTheRealms() {
        // language=GraphQL
        String query = """
                query {
                    realms {
                        items {
                            name
                        }
                    }
                }
                """;

        int numRealms = 5; // including master and test realms
        String[] realmNames = new String[numRealms];
        realmNames[0] = MASTER_REALM;
        realmNames[1] = TEST_REALM;
        for (int i = NUM_FIXTURE_REALMS; i < numRealms; i++) {
            String realmName = "realm" + i;
            createRealm(realmName);
            realmNames[i] = realmName;
        }
        // Only the admin in the master realm should be able to list all realms
        ValidatableResponse response = sendGraphQLRequestAsMasterAdmin(query);
        response.body("data.realms.items", hasSize(numRealms));
        response.body("data.realms.items.name", containsInAnyOrder(realmNames));
    }

    @Test
    public void shouldReturnNullForDefaultRealmForUnAuthorizedUser() {

        // language=GraphQL
        String query = """
                query {
                    realm {
                        name
                    }
                }
                """;

        UserResource user = createUser("baduser", "somepassword");
        assertNotNull(user);

        sendGraphQLRequestAsUser(query, TEST_REALM, "baduser", "somepassword").body("data.realm", equalTo(null));
    }

}
