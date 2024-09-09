package net.brianlevine.keycloak.graphql.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import net.brianlevine.keycloak.graphql.GraphQLController;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
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

//	@OPTIONS
//	@Path("{any:.*}")
//	public Response preflight() {
//		return Cors.builder().allowedOrigins("*").auth().preflight().add(Response.ok());
//	}



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
	public Response getForm() {
		LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class);
		return forms.createForm("graphiql.ftl");
	}
}
