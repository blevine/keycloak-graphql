package net.brianlevine.keycloak.graphql.rest;

import static org.junit.jupiter.api.Assertions.*;

import net.brianlevine.keycloak.graphql.GraphQLTest;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;

import static dasniko.testcontainers.keycloak.ExtendableKeycloakContainer.ADMIN_CLI_CLIENT;

public class GraphQLEndpointTest extends GraphQLTest {

	@Test
	public void testSchemaEndpoint() {
		AccessTokenResponse accessTokenResponse = testKeycloakClient.tokenManager().getAccessToken();

		givenSpec()
				.auth().oauth2(accessTokenResponse.getToken())
				.when().get("schemaAuth")
				.then().statusCode(200);
	}

	@Test
	public void testSchemaEndpointNoAuth() {
		givenSpec(MASTER_REALM).when().get("schemaAuth").then().statusCode(401);
	}

	@Test
	public void testSchemaEndpointUserNotAuthorized() {
		UserResource user = createUser("baduser", "somepassword");
		assertNotNull(user);

		Keycloak kc = Keycloak.getInstance(keycloak.getAuthServerUrl(), TEST_REALM, "baduser", "somepassword", ADMIN_CLI_CLIENT);

		AccessTokenResponse accessTokenResponse = kc.tokenManager().getAccessToken();

		givenSpec()
				.auth().oauth2(accessTokenResponse.getToken())
				.when().get("schemaAuth")
				.then().statusCode(403);
	}


}
