package net.brianlevine.keycloak.graphql.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import net.brianlevine.keycloak.graphql.GraphQLController;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class GraphQLResourceProvider implements RealmResourceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLResourceProvider.class);

	private final KeycloakSession session;
	private final GraphQLController graphql;

	public GraphQLResourceProvider(KeycloakSession session, GraphQLController graphql) {
		this.session = session;
		this.graphql = graphql;
	}

	@Override
	public Object getResource() {
		return this;
	}

	@Override
	public void close() {
	}


	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postGraphQL(Map<String, Object> body, @Context Request ctx) {
		//AuthResult auth = checkAuth();

		String query = (String)body.get("query");
		String operationName = (String)body.get("operationName");
		String variables = (String)body.get("variables");

		// TODO: Deal with variables.

		Map<String, Object> result = graphql.executeQuery(query, operationName, session);
		return Response.ok(result).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getGraphQL(Map<String, Object> body, @Context Request ctx) {
		//AuthResult auth = checkAuth();
		String message = "{\"status\": \"Hello GraphQL!\"}";

		return Response.ok(message).build();
	}


	@GET
	@Path("/form")
	@Produces(MediaType.TEXT_HTML)
	public Response getForm() {
		KeycloakContext context = session.getContext();

		LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class);
		forms.setAttribute("realm_name", context.getRealm().getName());

		return forms.createForm("graphiql.ftl");
	}




	private AuthResult checkAuth() {
		AuthResult auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
		if (auth == null) {
			throw new NotAuthorizedException("Bearer");
		} else if (auth.getToken().getIssuedFor() == null || !auth.getToken().getIssuedFor().equals("admin-cli")) {
			throw new ForbiddenException("Token is not properly issued for admin-cli");
		}
		return auth;
	}
}
