package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.UserFederationProviderRepresentation;

import java.util.Map;

@GraphQLType
@SuppressWarnings("unused")
public class UserFederationProviderType {
    private final UserFederationProviderRepresentation delegate;

    public UserFederationProviderType(UserFederationProviderRepresentation userFederationProviderRepresentation) {
        this.delegate = userFederationProviderRepresentation;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public void setDisplayName(String displayName) {
        delegate.setDisplayName(displayName);
    }

    public String getProviderName() {
        return delegate.getProviderName();
    }

    public void setProviderName(String providerName) {
        delegate.setProviderName(providerName);
    }

    @GraphQLQuery
    public AttributeMap getConfig(PagingOptions options) {
        return new AttributeMap(delegate.getConfig(), options);
    }

    public void setConfig(Map<String, String> config) {
        delegate.setConfig(config);
    }

    public int getPriority() {
        return delegate.getPriority();
    }

    public void setPriority(int priority) {
        delegate.setPriority(priority);
    }

    public int getFullSyncPeriod() {
        return delegate.getFullSyncPeriod();
    }

    public void setFullSyncPeriod(int fullSyncPeriod) {
        delegate.setFullSyncPeriod(fullSyncPeriod);
    }

    public int getChangedSyncPeriod() {
        return delegate.getChangedSyncPeriod();
    }

    public void setChangedSyncPeriod(int changedSyncPeriod) {
        delegate.setChangedSyncPeriod(changedSyncPeriod);
    }

    public int getLastSync() {
        return delegate.getLastSync();
    }

    public void setLastSync(int lastSync) {
        delegate.setLastSync(lastSync);
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
