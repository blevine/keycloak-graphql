package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;

import java.io.Serializable;
import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class AuthenticatorConfigType implements Serializable {
    private final AuthenticatorConfigRepresentation delegate;

    public AuthenticatorConfigType(AuthenticatorConfigRepresentation authenticatorConfigRepresentation) {
        this.delegate = authenticatorConfigRepresentation;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
    }

    public String getAlias() {
        return delegate.getAlias();
    }

    public void setAlias(String alias) {
        delegate.setAlias(alias);
    }

    @GraphQLQuery
    public AttributeMap getConfig(PagingOptions options) {
        return new AttributeMap(delegate.getConfig(), options);
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }
}
