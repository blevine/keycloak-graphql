package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class ProtocolMapperType {
    private final ProtocolMapperRepresentation delegate;

    public ProtocolMapperType(ProtocolMapperRepresentation protocolMapperRepresentation) {
        this.delegate = protocolMapperRepresentation;
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

    public String getProtocol() {
        return delegate.getProtocol();
    }

    public void setProtocol(String protocol) {
        delegate.setProtocol(protocol);
    }

    public String getProtocolMapper() {
        return delegate.getProtocolMapper();
    }

    public void setProtocolMapper(String protocolMapper) {
        delegate.setProtocolMapper(protocolMapper);
    }

    @GraphQLQuery
    public AttributeMap getConfig(PagingOptions options) {
        return new AttributeMap(delegate.getConfig(), options);
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }

    @Deprecated
    public boolean isConsentRequired() {
        return delegate.isConsentRequired();
    }

    @Deprecated
    public String getConsentText() {
        return delegate.getConsentText();
    }
}
