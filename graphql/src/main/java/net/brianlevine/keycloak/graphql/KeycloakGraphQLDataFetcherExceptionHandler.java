package net.brianlevine.keycloak.graphql;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLException;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import graphql.language.SourceLocation;

import jakarta.ws.rs.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KeycloakGraphQLDataFetcherExceptionHandler extends SimpleDataFetcherExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("KeycloakGraphQL");

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = unwrap(handlerParameters.getException());
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ResultPath path = handlerParameters.getPath();

        Map<String, Object> add = getExtensions(exception);

        KeycloakGraphQLError error = new KeycloakGraphQLError(path, exception, sourceLocation, add);

        logException(error, exception);

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private static Map<String, Object> getExtensions(Throwable exception) {
        Map<String, Object> add = new HashMap<>();
        String code = null;
        if (exception instanceof ExceptionWithCode) {
            code = ((ExceptionWithCode)exception).getCode().name();
        }
        else {
            String[] parts = exception.getClass().getName().split("\\.");
            code = parts[parts.length - 1];
        }

        add.put("code", code);
        return add;
    }


    protected void logException(ExceptionWhileDataFetching error, Throwable exception) {
        LOGGER.error(error.getMessage(), exception);
    }


}
