package net.brianlevine.keycloak.graphql;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import graphql.language.SourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakGraphQLDataFetcherExceptionHandler extends SimpleDataFetcherExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("KeycloakGraphQL");

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = unwrap(handlerParameters.getException());
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ResultPath path = handlerParameters.getPath();

        KeycloakGraphQLError error = new KeycloakGraphQLError(path, exception, sourceLocation);

        logException(error, exception);

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }


    protected void logException(ExceptionWhileDataFetching error, Throwable exception) {
        LOGGER.error(error.getMessage(), exception);
    }


}
