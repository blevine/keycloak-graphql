package net.brianlevine.keycloak.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.leangen.graphql.GraphQLRuntime;
import io.leangen.graphql.GraphQLSchemaGenerator;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.HttpHeaders;
import net.brianlevine.keycloak.graphql.queries.ErrorQuery;
import net.brianlevine.keycloak.graphql.queries.RealmQuery;
import net.brianlevine.keycloak.graphql.queries.UserQuery;
import net.brianlevine.keycloak.graphql.subscriptions.EventsSubscription;
import net.brianlevine.keycloak.graphql.util.OverrideTypeInfoGenerator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static net.brianlevine.keycloak.graphql.Constants.*;

import static net.brianlevine.keycloak.graphql.util.Auth.verifyAccessToken;
import static net.brianlevine.keycloak.graphql.util.Util.fakeHttpHeadersWithToken;

public class GraphQLController {

    private static GraphQL graphQL;

    public GraphQLController() {
    }

    public static GraphQL getSchema() {
        return getSchema(false);
    }

    public static GraphQL getSchema(boolean reset) {

        if (reset) {
            graphQL = null;
        }

        if (graphQL == null) {
            RealmQuery realmQuery = new RealmQuery();
            ErrorQuery errorQuery = new ErrorQuery();
            UserQuery userQuery = new UserQuery();
            EventsSubscription testSubscription = new EventsSubscription();

            //Schema generated from query classes
            GraphQLSchema schema = new GraphQLSchemaGenerator()
                    .withBasePackages("net.brianlevine.keycloak.graphql", "org.keycloak.events", "org.keycloak.events.admin")
                    .withOperationsFromSingletons(realmQuery, errorQuery, userQuery, testSubscription)
                    .withRelayConnectionCheckRelaxed()
                    .withTypeInfoGenerator(new OverrideTypeInfoGenerator().withHierarchicalNames(false))
                    .generate();

            DataFetcherExceptionHandler exceptionHandler = new KeycloakGraphQLDataFetcherExceptionHandler();
            graphQL = GraphQLRuntime
                    .newGraphQL(schema)
                    .defaultDataFetcherExceptionHandler(exceptionHandler)
                    .subscriptionExecutionStrategy(new KeycloakSubscriptionExecutionStrategy(exceptionHandler))
                    .build();

        }


        return graphQL;
    }

    public Map<String, Object> executeQueryToSpec(
            @Nonnull String query,
            String operationName,
            @Nonnull KeycloakSession session,
            @Nonnull String accessToken,
            Request request,
            HttpHeaders headers,
            Map<String, Object> variables,
            Map<String, Object> additionalContext) {

        return executeQuery(query, operationName, session, accessToken, request, headers, variables, additionalContext).toSpecification();
    }

    public ExecutionResult executeQuery(
            @Nonnull String query,
            String operationName,
            @Nonnull KeycloakSession session,
            @Nonnull String accessToken,
            Request request,
            HttpHeaders headers,
            Map<String, Object> variables,
            Map<String, Object> additionalContext) {

        Map<String, Object> ctx = new HashMap<>();

        if (additionalContext != null) {
            ctx.putAll(additionalContext);
        }

        Map<String, Object> v =  variables == null ? new HashMap<>(): variables;
        ctx.put(KEYCLOAK_SESSION_KEY, session);

        if (request != null) {
            ctx.put("request", request);
        }


        // validate token
        AccessToken at = verifyAccessToken(accessToken, session);

        if (at == null) {
            throw new NotAuthorizedException("Access token expired");
        }

        // No headers means that we're being called from the WebSocket server as a result of a subscription
        // Need to fake HTTP headers because some Keycloak resource classes require the Bearer token in order
        // to do authn and authz.

        HttpHeaders h = headers;
        if (h == null) {
            h = fakeHttpHeadersWithToken(accessToken);
        }

        ctx.put(HTTP_HEADERS_KEY, h);
        ctx.put(ACCESS_TOKEN_KEY, accessToken);

        ExecutionResult executionResult = getSchema().execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(v)
                .graphQLContext(ctx)
                .build());
        return executionResult;
    }


    public String printSchema() {

        return new SchemaPrinter(
                SchemaPrinter.Options.defaultOptions()
                        .includeDirectives(true)
                        .includeScalarTypes(true)
                        .includeSchemaDefinition(true)
                        .includeIntrospectionTypes(true)
        ).print(getSchema().getGraphQLSchema());
    }

}
