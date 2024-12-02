package net.brianlevine.keycloak.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.leangen.graphql.GraphQLRuntime;
import io.leangen.graphql.GraphQLSchemaGenerator;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.HttpHeaders;
import net.brianlevine.keycloak.graphql.queries.ErrorQuery;
import net.brianlevine.keycloak.graphql.queries.RealmQuery;
import net.brianlevine.keycloak.graphql.queries.UserQuery;
import net.brianlevine.keycloak.graphql.subscriptions.EventsSubscription;
import net.brianlevine.keycloak.graphql.util.OverrideTypeInfoGenerator;
import org.keycloak.models.KeycloakSession;

import java.util.HashMap;
import java.util.Map;

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
            String query,
            String operationName,
            KeycloakSession session,
            String accessToken,
            Request request,
            HttpHeaders headers,
            Map<String, Object> variables) {

        return executeQuery(query, operationName, session, accessToken, request, headers, variables).toSpecification();
    }

    public ExecutionResult executeQuery(
            String query,
            String operationName,
            KeycloakSession session,
            String accessToken,
            Request request,
            HttpHeaders headers,
            Map<String, Object> variables) {

        Map<String, Object> ctx = new HashMap<>();

        Map<String, Object> v =  variables == null ? new HashMap<>(): variables;

        if (session != null) {
            ctx.put("keycloak.session", session);
        }

        if (request != null) {
            ctx.put("request", request);
        }

        if (headers != null) {
            ctx.put("headers", headers);
        }

        if (accessToken != null) {
            ctx.put("access_token", accessToken);
        }

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
