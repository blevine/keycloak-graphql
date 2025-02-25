package net.brianlevine.keycloak.graphql;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import net.brianlevine.keycloak.graphql.rest.GraphQLResourceProviderFactory;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.util.List;

import static dasniko.testcontainers.keycloak.ExtendableKeycloakContainer.ADMIN_CLI_CLIENT;
import static io.restassured.RestAssured.given;

public abstract class KeycloakGraphQLTest {
    public final static String TEST_REALM = "test";
    public final static String MASTER_REALM = "master";
    public final static int NUM_FIXTURE_REALMS = 2;

    private final static String KEYCLOAK_VERSION = "26.0.2";
    private final static String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION;


    public static KeycloakContainer keycloak;
    public static Keycloak masterKeycloakClient;
    public static Keycloak testKeycloakClient;
    public static RealmResource masterRealmResource;
    public static RealmResource testRealmResource;
    public static UserResource masterRealmAdminUser;



    @BeforeAll
    public static void beforeAll() {
        if (keycloak == null) {
            final List<File> libs = Maven.resolver()
                    .loadPomFromFile("./pom.xml")
                    .importCompileAndRuntimeDependencies()
                    .resolve()
                    .withTransitivity()
                    .asList(File.class);
//                    .resolve(
//                            "io.leangen.graphql:spqr:0.12.3",
//                            "org.glassfish.tyrus:tyrus-server:2.1.5",
//                            "org.glassfish.tyrus:tyrus-container-grizzly-server:2.1.5",
//                            "io.projectreactor:reactor-core:3.6.8",
//                            "org.slf4j:slf4j-reload4j:2.0.6"
//                            )
//                    .withTransitivity().asList(File.class);

            String debugPort = System.getenv().get("DEBUG_PORT");
            if (debugPort != null) {
                keycloak = new KeycloakContainer(KEYCLOAK_IMAGE)
                        .withProviderClassesFrom("target/classes")
                        .withProviderLibsFrom(libs)
                        .withEnv("DB_VENDOR", "h2")
                        .withDebugFixedPort(Integer.parseInt(debugPort), true);
                System.out.println(">>> Keycloak debugging with wait-for-attach is ENABlED. Debug port = " + debugPort);
            } else {
                keycloak = new KeycloakContainer(KEYCLOAK_IMAGE)
                        .withProviderClassesFrom("target/classes")
                        .withProviderLibsFrom(libs)
                        .withEnv("DB_VENDOR", "h2");
            }
            keycloak.start();
            masterKeycloakClient = keycloak.getKeycloakAdminClient();
            masterRealmResource = masterKeycloakClient.realm(MASTER_REALM);
            UserRepresentation ur = masterRealmResource.users().searchByUsername("admin", true).get(0);
            masterRealmAdminUser = masterRealmResource.users().get(ur.getId());
        }
    }


    @BeforeEach
    public void before() {

        // Create the test realm
        testRealmResource = createRealm(TEST_REALM);

        // Create the admin user in the test realm
        UserResource admin = createUser("admin", "admin");

        // Create an admin client using the admin user's credentials
        testKeycloakClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), TEST_REALM, "admin", "admin", ADMIN_CLI_CLIENT);

        // Give the admin user realm management rights
        addClientRoleToUser(admin, "realm-management", "realm-admin");

        // Create the graphql-tools realm role and give the admin user that role
        RoleRepresentation role = createRealmRole(GraphQLResourceProviderFactory.GRAPHQL_TOOLS_ROLE, "").toRepresentation();
        addRealmRoleToUser(admin, GraphQLResourceProviderFactory.GRAPHQL_TOOLS_ROLE);
    }

    @AfterEach
    public void after() {
        deleteRealms();
    }

    public static UserResource createUser(String username, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setFirstName(username);
        user.setLastName(username);
        user.setUsername(username);
        user.setEnabled(true);
        user.setEmail(username + "@example.com");
        user.setEmailVerified(true);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));

        // TODO: can we get the id from the Location header in the response.
        testRealmResource.users().create(user);

        user = testRealmResource.users().search(username).get(0);
        return testRealmResource.users().get(user.getId());

    }

    public static void addRealmRoleToUser(UserResource user, String roleName) {
        RoleResource roleResource = testRealmResource.roles().get(roleName);

        if (roleResource != null) {
            user.roles().realmLevel().add(List.of(roleResource.toRepresentation()));
        }
        else {
            throw new RuntimeException("Role " + roleName + " not found");
        }
    }

    public static void addClientRoleToUser(UserResource user, String clientName, String roleName) {
        ClientRepresentation cr = testRealmResource.clients().findByClientId(clientName).get(0);
        ClientResource clientResource = testRealmResource.clients().get(cr.getId());
        RoleResource roleResource = clientResource.roles().get(roleName);

        if (roleResource != null) {
            user.roles().clientLevel(cr.getId()).add(List.of(roleResource.toRepresentation()));
        }
        else {
            throw new RuntimeException("Role " + roleName + " not found");
        }
    }

    public static RoleResource createRealmRole(String roleName, String description) {
        RoleRepresentation role = new RoleRepresentation(roleName, description, false);
        role.setClientRole(false);
        testRealmResource.roles().create(role);

        return testRealmResource.roles().get(roleName);
    }

    public static UserResource getUserResourceByUsername(String realmName, String username) {
        RealmResource realm = testKeycloakClient.realm(realmName);
        List<UserRepresentation> users = realm.users().search(username, true);
        UserRepresentation admin = users.get(0);

        return admin != null ? testRealmResource.users().get(admin.getId()) : null;
    }

    public static ClientRepresentation createClient(String clientName) {
        ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setClientId(clientName);
        clientRep.setSecret("secret");
        clientRep.setEnabled(true);
        clientRep.setDirectAccessGrantsEnabled(true);
        testRealmResource.clients().create(clientRep);
        return testRealmResource.clients().findByClientId(clientName).get(0);
    }

    public static RealmResource createRealm(String realmName) {
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setEnabled(true);
        masterKeycloakClient.realms().create(realm);

        return masterKeycloakClient.realms().realm(realmName);
    }

    /**
     * Delete all realms except for master
     *
     */
    public void deleteRealms() {
        masterKeycloakClient.realms().findAll().forEach(realm -> {
            if (!realm.getRealm().equals("master")) {
                masterKeycloakClient.realm(realm.getRealm()).remove();
            }

        });
    }

    public ValidatableResponse sendGraphQLRequestAsMasterAdmin(String graphQLRequest) {
        return sendGraphQLRequestAsUser(graphQLRequest, MASTER_REALM, "admin", "admin");
    }

    public ValidatableResponse sendGraphQLRequestAsTestAdmin(String graphQLRequest) {
        return sendGraphQLRequestAsUser(graphQLRequest, TEST_REALM, "admin", "admin");
    }

    public ValidatableResponse sendGraphQLRequestAsUser(String graphQLRequest, String realmName, String username, String password) {
        return sendGraphQLRequestAsUser(graphQLRequest, realmName, username, password, 200);
    }

    public ValidatableResponse sendGraphQLRequestAsUser(String graphQLRequest, String realmName, String username, String password, int expectedStatusCode) {
        Keycloak kc = Keycloak.getInstance(keycloak.getAuthServerUrl(),realmName, username, password, ADMIN_CLI_CLIENT);

        AccessTokenResponse accessTokenResponse = kc.tokenManager().getAccessToken();


        JSONObject json = new JSONObject();
        json.put("query", graphQLRequest);

        ValidatableResponse response = givenSpec(realmName)
                .auth().oauth2(accessTokenResponse.getToken())
                .contentType("application/json")
                .body(json.toJSONString())
                .when().post()
                .then().statusCode(expectedStatusCode);

        return response;
    }

    protected RequestSpecification givenSpec() {
        return givenSpec(TEST_REALM);
    }
    protected RequestSpecification givenSpec(String realmName) {
        return given().baseUri(keycloak.getAuthServerUrl()).basePath("/realms/" + realmName + "/" + GraphQLResourceProviderFactory.PROVIDER_ID);
    }

}
