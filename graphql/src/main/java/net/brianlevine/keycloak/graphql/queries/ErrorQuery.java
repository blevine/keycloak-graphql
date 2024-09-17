package net.brianlevine.keycloak.graphql.queries;

import io.leangen.graphql.annotations.GraphQLQuery;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class ErrorQuery {
    @GraphQLQuery
    public Object getForbiddenException() {
        throw new ForbiddenException();
    }

    @GraphQLQuery
    public Object getNotFoundException() {
        throw new NotFoundException();
    }

    @GraphQLQuery
    public Object getNotAuthorizedError() {
        throw new NotAuthorizedException(Response.status(401).build());
    }

    @GraphQLQuery
    public Object getRuntimeError() {
        throw new RuntimeException("This is a runtime exception");
    }

    @GraphQLQuery
    public Object getException() throws Exception {
        throw new Exception("This is a standard exception");
    }
}
