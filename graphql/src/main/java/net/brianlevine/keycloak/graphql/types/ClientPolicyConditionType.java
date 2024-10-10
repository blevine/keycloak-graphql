package net.brianlevine.keycloak.graphql.types;

import com.fasterxml.jackson.databind.JsonNode;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.ClientPolicyConditionRepresentation;

import java.util.Objects;

@GraphQLType
@SuppressWarnings("unused")
public class ClientPolicyConditionType {
    private final ClientPolicyConditionRepresentation delegate;

    public ClientPolicyConditionType(ClientPolicyConditionRepresentation clientPolicyConditionRepresentation) {
        this.delegate = clientPolicyConditionRepresentation;
    }

    public String getConditionProviderId() {
        return delegate.getConditionProviderId();
    }

    public void setConditionProviderId(String conditionProviderId) {
        delegate.setConditionProviderId(conditionProviderId);
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
