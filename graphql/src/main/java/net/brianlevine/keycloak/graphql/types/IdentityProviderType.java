package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class IdentityProviderType {
    private final IdentityProviderRepresentation delegate;

    public IdentityProviderType(IdentityProviderRepresentation delegate) {
        this.delegate = delegate;
    }

    public String getInternalId() {
        return delegate.getInternalId();
    }

    public void setInternalId(String internalId) {
        delegate.setInternalId(internalId);
    }

    public String getAlias() {
        return delegate.getAlias();
    }

    public void setAlias(String alias) {
        delegate.setAlias(alias);
    }

    public String getProviderId() {
        return delegate.getProviderId();
    }

    public void setProviderId(String providerId) {
        delegate.setProviderId(providerId);
    }

    public Map<String, String> getConfig() {
        return delegate.getConfig();
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public boolean isLinkOnly() {
        return delegate.isLinkOnly();
    }

    public void setLinkOnly(boolean linkOnly) {
        delegate.setLinkOnly(linkOnly);
    }


    public String getFirstBrokerLoginFlowAlias() {
        return delegate.getFirstBrokerLoginFlowAlias();
    }

    public void setFirstBrokerLoginFlowAlias(String firstBrokerLoginFlowAlias) {
        delegate.setFirstBrokerLoginFlowAlias(firstBrokerLoginFlowAlias);
    }

    public String getPostBrokerLoginFlowAlias() {
        return delegate.getPostBrokerLoginFlowAlias();
    }

    public void setPostBrokerLoginFlowAlias(String postBrokerLoginFlowAlias) {
        delegate.setPostBrokerLoginFlowAlias(postBrokerLoginFlowAlias);
    }

    public boolean isStoreToken() {
        return delegate.isStoreToken();
    }

    public void setStoreToken(boolean storeToken) {
        delegate.setStoreToken(storeToken);
    }

    public boolean isAddReadTokenRoleOnCreate() {
        return delegate.isAddReadTokenRoleOnCreate();
    }

    public void setAddReadTokenRoleOnCreate(boolean addReadTokenRoleOnCreate) {
        delegate.setAddReadTokenRoleOnCreate(addReadTokenRoleOnCreate);
    }

    public boolean isTrustEmail() {
        return delegate.isTrustEmail();
    }

    public void setTrustEmail(boolean trustEmail) {
        delegate.setTrustEmail(trustEmail);
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public void setDisplayName(String displayName) {
        delegate.setDisplayName(displayName);
    }
}
