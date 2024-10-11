package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;

import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class RequiredActionProviderType {
    private final RequiredActionProviderRepresentation delegate;

    public RequiredActionProviderType(RequiredActionProviderRepresentation requiredActionProviderRepresentation) {
        this.delegate = requiredActionProviderRepresentation;
    }

    public String getAlias() {
        return delegate.getAlias();
    }

    public void setAlias(String alias) {
        delegate.setAlias(alias);
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public boolean isDefaultAction() {
        return delegate.isDefaultAction();
    }

    public void setDefaultAction(boolean defaultAction) {
        delegate.setDefaultAction(defaultAction);
    }

    public String getProviderId() {
        return delegate.getProviderId();
    }

    public void setProviderId(String providerId) {
        delegate.setProviderId(providerId);
    }

    public int getPriority() {
        return delegate.getPriority();
    }

    public void setPriority(int priority) {
        delegate.setPriority(priority);
    }

    @GraphQLQuery
    public AttributeMap getConfig(PagingOptions options) {
        return new AttributeMap(delegate.getConfig(), options);
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }
}
