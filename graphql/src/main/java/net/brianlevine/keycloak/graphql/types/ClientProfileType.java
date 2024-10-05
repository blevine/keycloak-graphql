package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.representations.idm.ClientPolicyExecutorRepresentation;
import org.keycloak.representations.idm.ClientProfileRepresentation;

import java.util.List;
import java.util.Objects;

@GraphQLType
@SuppressWarnings("unused")
public class ClientProfileType {
    private final ClientProfileRepresentation delegate;

    public ClientProfileType(ClientProfileRepresentation clientProfileRepresentation) {
        this.delegate = clientProfileRepresentation;
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public Page<ClientPolicyExecutorType> getExecutors(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<ClientPolicyExecutorRepresentation> executors = delegate.getExecutors();
        List<ClientPolicyExecutorType> ets = executors.stream()
                .skip(start)
                .limit(limit)
                .map(ClientPolicyExecutorType::new)
                .toList();

        return new Page<>(executors.size(), limit, ets);
    }

    public void setExecutors(List<ClientPolicyExecutorRepresentation> executors) {
        delegate.setExecutors(executors);
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
