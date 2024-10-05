package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.List;
import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class ClientScopeType {
    private final ClientScopeRepresentation delegate;

    public ClientScopeType(ClientScopeRepresentation clientScopeRepresentation) {
        this.delegate = clientScopeRepresentation;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
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

    public Page<ProtocolMapperType> getProtocolMappers(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<ProtocolMapperType> pms = delegate.getProtocolMappers().stream()
                .skip(start)
                .limit(limit)
                .map(ProtocolMapperType::new)
                .toList();

        return new Page<>(pms.size(), limit, pms);
    }

    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
        delegate.setProtocolMappers(protocolMappers);
    }

    public String getProtocol() {
        return delegate.getProtocol();
    }

    public void setProtocol(String protocol) {
        delegate.setProtocol(protocol);
    }

    public AttributeMap getAttributes(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        return new AttributeMap(delegate.getAttributes(), start, limit);
    }

    public void setAttributes(Map<String, String> attributes) {
        delegate.setAttributes(attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientScopeRepresentation)) return false;
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
