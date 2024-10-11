package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;

import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class IdentityProviderMapperType {
    private final IdentityProviderMapperRepresentation delegate;

    public IdentityProviderMapperType(IdentityProviderMapperRepresentation identityProviderMapperRepresentation) {
        this.delegate = identityProviderMapperRepresentation;
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

    public String getIdentityProviderAlias() {
        return delegate.getIdentityProviderAlias();
    }

    public void setIdentityProviderAlias(String identityProviderAlias) {
        delegate.setIdentityProviderAlias(identityProviderAlias);
    }

    public String getIdentityProviderMapper() {
        return delegate.getIdentityProviderMapper();
    }

    public void setIdentityProviderMapper(String identityProviderMapper) {
        delegate.setIdentityProviderMapper(identityProviderMapper);
    }

    @GraphQLQuery
    public AttributeMap getConfig(PagingOptions options) {
        return new AttributeMap(delegate.getConfig(), options);
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }
}
