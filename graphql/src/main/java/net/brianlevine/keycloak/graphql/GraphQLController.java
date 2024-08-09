package net.brianlevine.keycloak.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.geantyref.TypeToken;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import net.brianlevine.keycloak.graphql.queries.PersonQuery;
import net.brianlevine.keycloak.graphql.queries.RealmQuery;
import net.brianlevine.keycloak.graphql.types.RealmType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.RealmModelDelegate;

import java.util.HashMap;
import java.util.Map;

public class GraphQLController {

    private final GraphQL graphQL;

    // TODO: Maybe call initialization from the REST provider factory
    public GraphQLController() {
        PersonQuery personQuery = new PersonQuery();
        RealmQuery realmQuery = new RealmQuery();

        //Schema generated from query classes
        GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withBasePackages(
                        "net.brianlevine.graphql"
                )
                .withOperationsFromSingletons(realmQuery)
                .withRelayConnectionCheckRelaxed()
                .generate();

        graphQL = GraphQL.newGraphQL(schema).build();
    }

    public Map<String, Object> executeQuery(String query, String operationName, KeycloakSession session, Map<String, Object> variables) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("keycloak.session", session);
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .graphQLContext(ctx)
                .build());
        return executionResult.toSpecification();
    }

    public Map<String, Object> executeQuery(String query, String operationName, KeycloakSession session) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("keycloak.session", session);
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .graphQLContext(ctx)
                .build());
        return executionResult.toSpecification();
    }
}
