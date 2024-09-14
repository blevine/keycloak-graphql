package net.brianlevine.keycloak.graphql.rest;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.util.List;

public abstract class GraphQLTest {
    private final static String KEYCLOAK_VERSION = "25.0.2";
    private final static String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION;
    private final static List<File> libs = Maven.resolver()
            .resolve("io.leangen.graphql:spqr:0.12.3")
            .withTransitivity().asList(File.class);

    @Container
    public static final KeycloakContainer keycloak = new KeycloakContainer(KEYCLOAK_IMAGE)
            .withProviderClassesFrom("target/classes")
            .withProviderLibsFrom(libs);

    public static Keycloak keycloakClient;
    public static RealmResource realmResource;

    @BeforeAll
    public static void beforeAll() {
        keycloakClient = keycloak.getKeycloakAdminClient();
        realmResource = keycloakClient.realm("master");

        setupAdminUser();
    }

    public static UserResource createUser(String username, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));

        // TODO: can we get the id from the Location header in the response.
        realmResource.users().create(user);

        user = realmResource.users().search(username).get(0);
        return realmResource.users().get(user.getId());

    }

    public static void setupAdminUser() {
        UserResource adminUserResource = getUserResourceByUsername("admin");
        createRealmRole(GraphQLResourceProviderFactory.GRAPHQL_TOOLS_ROLE, "");
        addRealmRoleToUser(adminUserResource, GraphQLResourceProviderFactory.GRAPHQL_TOOLS_ROLE);
    }


    public static void addRealmRoleToUser(UserResource user, String roleName) {
        RoleResource roleResource = realmResource.roles().get(roleName);

        if (roleResource != null) {
            user.roles().realmLevel().add(List.of(roleResource.toRepresentation()));
        }
        else {
            throw new RuntimeException("Role " + roleName + " not found");
        }
    }

    public static RoleRepresentation createRealmRole(String roleName, String description) {
        RoleRepresentation role = new RoleRepresentation(roleName, description, false);
        role.setClientRole(false);
        realmResource.roles().create(role);
        role = realmResource.roles().get(roleName).toRepresentation();

        return role;
    }

    public static UserResource getUserResourceByUsername(String username) {
        List<UserRepresentation> users = realmResource.users().search(username, true);
        UserRepresentation admin = users.get(0);

        return admin != null ? realmResource.users().get(admin.getId()) : null;
    }

    public static ClientRepresentation createClient(String clientName) {
        ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setClientId(clientName);
        clientRep.setSecret("secret");
        clientRep.setEnabled(true);
        clientRep.setDirectAccessGrantsEnabled(true);
        realmResource.clients().create(clientRep);
        return realmResource.clients().findByClientId(clientName).get(0);
    }

}
