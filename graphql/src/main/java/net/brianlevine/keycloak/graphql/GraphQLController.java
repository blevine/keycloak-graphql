package net.brianlevine.keycloak.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.leangen.graphql.GraphQLRuntime;
import io.leangen.graphql.GraphQLSchemaGenerator;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.HttpHeaders;
import net.brianlevine.keycloak.graphql.queries.ErrorQuery;
import net.brianlevine.keycloak.graphql.queries.RealmQuery;
import net.brianlevine.keycloak.graphql.queries.UserQuery;
import net.brianlevine.keycloak.graphql.util.OverrideTypeInfoGenerator;
import org.keycloak.models.KeycloakSession;

import java.util.HashMap;
import java.util.Map;

public class GraphQLController {

    private GraphQL graphQL;

    public GraphQLController() {
    }

    private GraphQL getSchema() {

        if (graphQL == null) {
            RealmQuery realmQuery = new RealmQuery();
            ErrorQuery errorQuery = new ErrorQuery();
            UserQuery userQuery = new UserQuery();

            //Schema generated from query classes
            GraphQLSchema schema = new GraphQLSchemaGenerator()
                    .withBasePackages("net.brianlevine.keycloak.graphql")
                    .withOperationsFromSingletons(realmQuery, errorQuery, userQuery)
                    .withRelayConnectionCheckRelaxed()
                    .withTypeInfoGenerator(new OverrideTypeInfoGenerator().withHierarchicalNames(false))
                    .generate();

            graphQL = GraphQLRuntime
                    .newGraphQL(schema)
                    .defaultDataFetcherExceptionHandler(new KeycloakGraphQLDataFetcherExceptionHandler())
                    .build();

        }


        return graphQL;
    }

    public Map<String, Object> executeQuery(String query, String operationName, KeycloakSession session, Request request, HttpHeaders headers, Map<String, Object> variables) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("keycloak.session", session);
        ctx.put("request", request);
        ctx.put("headers", headers);

        ExecutionResult executionResult = getSchema().execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .graphQLContext(ctx)
                .build());
        return executionResult.toSpecification();
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
