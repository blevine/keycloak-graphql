package net.brianlevine.keycloak.graphql.rest;

import io.restassured.specification.RequestSpecification;

import org.junit.Assert;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Config;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dasniko.testcontainers.keycloak.ExtendableKeycloakContainer.ADMIN_CLI_CLIENT;
import static io.restassured.RestAssured.given;

@Testcontainers
public class GraphQLEndpointTest extends GraphQLTest {



	@Test
	public void testSchemaEndpoint() {
		AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();

		givenSpec()
				.auth().oauth2(accessTokenResponse.getToken())
				.when().get("schemaAuth")
				.then().statusCode(200);
	}

	@Test
	public void testSchemaEndpointNoAuth() {
		givenSpec().when().get("schemaAuth").then().statusCode(401);
	}

	@Test
	public void testSchemaEndpointUserNotAuthorized() {
		UserResource user = createUser("baduser", "somepassword");
		assertNotNull(user);

		Keycloak kc = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", "baduser", "somepassword", ADMIN_CLI_CLIENT);

		AccessTokenResponse accessTokenResponse = kc.tokenManager().getAccessToken();

		givenSpec()
				.auth().oauth2(accessTokenResponse.getToken())
				.when().get("schemaAuth")
				.then().statusCode(403);
	}

	private RequestSpecification givenSpec() {
		return given().baseUri(keycloak.getAuthServerUrl()).basePath("/realms/master/" + GraphQLResourceProviderFactory.PROVIDER_ID);
	}
}
