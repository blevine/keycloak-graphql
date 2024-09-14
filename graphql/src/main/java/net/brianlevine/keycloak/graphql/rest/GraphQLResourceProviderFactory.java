package net.brianlevine.keycloak.graphql.rest;

import com.google.auto.service.AutoService;
import net.brianlevine.keycloak.graphql.GraphQLController;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@AutoService(RealmResourceProviderFactory.class)
public class GraphQLResourceProviderFactory implements RealmResourceProviderFactory {

	public static final String PROVIDER_ID = "graphql";
	public static final String GRAPHQL_TOOLS_ROLE = "graphql-tools";

	private GraphQLController graphql;

	@Override
	public RealmResourceProvider create(KeycloakSession keycloakSession) {
		return new GraphQLResourceProvider(keycloakSession, graphql);
	}

	@Override
	public void init(Config.Scope scope) {
		graphql = new GraphQLController();
	}

	@Override
	public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
	}

	@Override
	public void close() {
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}
}
