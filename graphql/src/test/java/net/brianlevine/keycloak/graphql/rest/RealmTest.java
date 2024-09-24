package net.brianlevine.keycloak.graphql.rest;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UserResource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class RealmTest extends GraphQLTest {

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

        sendGraphQLRequestAsTestAdmin(query).body("data.realm.name", equalTo("test"));
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
                .body("data.realms.items.name[0]", equalTo("test"));
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

        int numRealms = 5; // including master realm
        String[] realmNames = new String[numRealms];
        realmNames[0] = "master";
        realmNames[1] = "test";
        for (int i = 2; i < numRealms; i++) {
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

        sendGraphQLRequestAsUser(query, "test", "baduser", "somepassword").body("data.realm", equalTo(null));
    }

}
