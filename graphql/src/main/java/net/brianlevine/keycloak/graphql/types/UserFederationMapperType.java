package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.UserFederationMapperRepresentation;

import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class UserFederationMapperType {
    private final UserFederationMapperRepresentation delegate;

    public UserFederationMapperType(UserFederationMapperRepresentation delegate) {
        this.delegate = delegate;
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

    public String getFederationProviderDisplayName() {
        return delegate.getFederationProviderDisplayName();
    }

    public void setFederationProviderDisplayName(String federationProviderDisplayName) {
        delegate.setFederationProviderDisplayName(federationProviderDisplayName);
    }

    public String getFederationMapperType() {
        return delegate.getFederationMapperType();
    }

    public void setFederationMapperType(String federationMapperType) {
        delegate.setFederationMapperType(federationMapperType);
    }

    @GraphQLQuery
    public AttributeMap getConfig(PagingOptions options) {
        return new AttributeMap(delegate.getConfig(), options);
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }
}
