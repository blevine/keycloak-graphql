package net.brianlevine.keycloak.graphql.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import net.brianlevine.keycloak.graphql.GraphQLController;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
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

		LOGGER.debug("Created GraphQLResourceProvider");
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
	public Response postGraphQL(Map<String, Object> body, @Context Request request, @Context HttpHeaders headers) throws JsonProcessingException {
		String query = (String)body.get("query");
		String operationName = (String)body.get("operationName");
		//String variables = (String)body.get("variables");

		// TODO: Deal with variables.

		Map<String, Object> result = graphql.executeQuery(query, operationName, session, request, headers);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
		String s = mapper.writeValueAsString(result);

		//return Response.ok(s).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials", "true").build();
		return Response.ok(s).build();
	}

	@GET
	@Path("/graphiql")
	@Produces(MediaType.TEXT_HTML)
	public Response getGraphiQL() {
		LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class);
		return forms.createForm("graphiql.ftl");
	}

	@GET
	@Path("/schema")
	@Produces(MediaType.TEXT_HTML)
	public Response schemaForm() {
		LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class);
		return forms.createForm("schema.ftl");
	}

	@GET
	@Path("/schemaAuth")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSchema() {
		AuthenticationManager.AuthResult authResult = checkAuth();

		UserModel user = authResult.getUser();
		user = session.users().getUserById(session.getContext().getRealm(), user.getId());

		LOGGER.info("Roles for user = {}", user.getRealmRoleMappingsStream().map(RoleModel::getName).toArray());
		RoleModel requiredRole = session
				.roles()
				.getRealmRole(session.getContext().getRealm(), GraphQLResourceProviderFactory.GRAPHQL_TOOLS_ROLE);

		if (requiredRole != null) {
			LOGGER.info("Required role is: {}", requiredRole.getName());
			LOGGER.info("User has role: {}", user.hasRole(requiredRole));
		}
		else {
			LOGGER.info("Required role is null");
		}

		if ((requiredRole != null) && user.hasRole(requiredRole)) {
			String schema = graphql.printSchema().replace("`","'");
			return Response.ok(schema).build();
		}
		else {
			throw new ForbiddenException("User does not have required role.");
		}

	}

	private AuthenticationManager.AuthResult checkAuth() {
		AuthenticationManager.AuthResult auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
		if (auth == null) {
			throw new NotAuthorizedException("Bearer");
		}

		return auth;
	}
}
