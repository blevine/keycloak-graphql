package net.brianlevine.keycloak.graphql.apollo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;

public class SubscribePayload {
    private  String operationName;
    private  String query;
    private  Map<String, Object> variables;
    private  Map<String, Object> extensions;

    public SubscribePayload(){}

    public SubscribePayload(String operationName, String query, Map<String, Object> variables, Map<String, Object> extensions) {
        this.operationName = operationName;
        this.query = query;
        this.variables = variables;
        this.extensions = extensions;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SubscribePayload
                && operationName.equals(((SubscribePayload) other).operationName)
                && query.equals(((SubscribePayload) other).query)
                && variables.equals(((SubscribePayload) other).variables)
                && extensions.equals(((SubscribePayload) other).extensions);
    }


}
