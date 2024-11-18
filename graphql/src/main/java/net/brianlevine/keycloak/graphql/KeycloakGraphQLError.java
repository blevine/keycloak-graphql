package net.brianlevine.keycloak.graphql;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;

import java.util.HashMap;
import java.util.Map;

public class KeycloakGraphQLError extends ExceptionWhileDataFetching {
    private Map<String, Object> additionalExtensions;

    public KeycloakGraphQLError(ResultPath path, Throwable exception, SourceLocation sourceLocation) {
        super(path, exception, sourceLocation);
    }

    public KeycloakGraphQLError(ResultPath path, Throwable exception, SourceLocation sourceLocation, Map<String, Object> additionalExtensions) {
        super(path, exception, sourceLocation);
        setAdditionalExtensions(additionalExtensions);
    }

    public void setAdditionalExtensions(Map<String, Object> additionalExtensions) {
        this.additionalExtensions = additionalExtensions;
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> allExtensions = new HashMap<>();
        Map<String, Object> extensions = super.getExtensions();

        if (extensions != null) {
            allExtensions.putAll(extensions);
        }

        if (additionalExtensions != null) {
            allExtensions.putAll(additionalExtensions);
        }

        return allExtensions;
    }

    @Override
    public String toString() {
        return "KeycloakGraphQLError{" +
                "path=" + getPath() +
                ", exception=" + getException() +
                ", locations=" + getLocations() +
                ", extensions=" + getExtensions() + //TODO: Default toString of HashMap OK?
                '}';
    }
}
