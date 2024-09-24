package net.brianlevine.keycloak.graphql.rest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dasniko.testcontainers.keycloak.ExtendableKeycloakContainer.ADMIN_CLI_CLIENT;

@Testcontainers
public class GraphQLEndpointTest extends GraphQLTest {

	@Test
	public void testSchemaEndpoint() {
		AccessTokenResponse accessTokenResponse = testKeycloakClient.tokenManager().getAccessToken();

		givenSpec("test")
				.auth().oauth2(accessTokenResponse.getToken())
				.when().get("schemaAuth")
				.then().statusCode(200);
	}

	@Test
	public void testSchemaEndpointNoAuth() {
		givenSpec("master").when().get("schemaAuth").then().statusCode(401);
	}

	@Test
	public void testSchemaEndpointUserNotAuthorized() {
		UserResource user = createUser("baduser", "somepassword");
		assertNotNull(user);

		Keycloak kc = Keycloak.getInstance(keycloak.getAuthServerUrl(), "test", "baduser", "somepassword", ADMIN_CLI_CLIENT);

		AccessTokenResponse accessTokenResponse = kc.tokenManager().getAccessToken();

		givenSpec("test")
				.auth().oauth2(accessTokenResponse.getToken())
				.when().get("schemaAuth")
				.then().statusCode(403);
	}


}
