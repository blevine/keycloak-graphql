package net.brianlevine.keycloak.graphql.mutations;

import io.restassured.response.ValidatableResponse;
import net.brianlevine.keycloak.graphql.KeycloakGraphQLTest;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.UserResource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

class UserMutationTest extends KeycloakGraphQLTest {

    @Test
    void shouldCreateUser() {
        String testUserName = "testUser";
        String testPassword = "testPassword";
        String testEmail = "testEmail@test.com";
        String testFirstName = "testFirstName";
        String testLastName = "testLastName";

        // language=GraphQL
        String mutation = String.format("""
                mutation {
                   createUser(user: {
                     username: "%s",
                     firstName: "%s",
                     lastName: "%s",
                     email: "%s",
                     password: "%s",
                   }) {
                     id
                     username
                     firstName
                     lastName
                     email
                   }
                 }
            """, testUserName, testFirstName, testLastName, testEmail, testPassword);

        ValidatableResponse response = sendGraphQLRequestAsTestAdmin(mutation);
        response.log().all();

        // Keycloak lower-cases user name and email
        response.body("data.createUser.username", equalTo(testUserName.toLowerCase()));
        response.body("data.createUser.firstName", equalTo(testFirstName));
        response.body("data.createUser.lastName", equalTo(testLastName));
        response.body("data.createUser.email", equalTo(testEmail.toLowerCase()));
        response.body("data.createUser.id", notNullValue());
    }

    @Test
    void shouldNotAllowDuplicateUsername() {
        String testUserName = "testUser";

        UserResource userResource = createUser(testUserName, "testPassword");

        // language=GraphQL
        String mutation = String.format("""
                mutation {
                   createUser(user: {
                     username: "%s"
                   }) {
                     id
                   }
                 }
            """, testUserName);

        ValidatableResponse response = sendGraphQLRequestAsTestAdmin(mutation);
        response.log().all();
    }
}