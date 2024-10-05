package net.brianlevine.keycloak.graphql.types;

import com.fasterxml.jackson.databind.JsonNode;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.ClientPolicyExecutorRepresentation;

import java.util.Objects;

@GraphQLType
@SuppressWarnings("unused")
public class ClientPolicyExecutorType {
    private final ClientPolicyExecutorRepresentation delegate;

    public ClientPolicyExecutorType(ClientPolicyExecutorRepresentation clientPolicyExecutorRepresentation) {
        this.delegate = clientPolicyExecutorRepresentation;
    }

    public String getExecutorProviderId() {
        return delegate.getExecutorProviderId();
    }

    public void setExecutorProviderId(String providerId) {
        delegate.setExecutorProviderId(providerId);
    }

    public JsonNode getConfiguration() {
        return delegate.getConfiguration();
    }

    public void setConfiguration(JsonNode configuration) {
        delegate.setConfiguration(configuration);
    }

    @Override
    public boolean equals(Object o) {
        if (delegate.getClass() != o.getClass()) return false;
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
