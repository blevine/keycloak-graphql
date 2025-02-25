package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLIgnore;
import org.keycloak.representations.idm.CredentialRepresentation;

public class CredentialType {
    private final CredentialRepresentation delegate;

    public CredentialType(CredentialRepresentation delegate) {
        this.delegate = delegate;
    }

    public CredentialRepresentation getCredentialRepresentation() {
        return delegate;
    }

    public void setTemporary(Boolean temporary) {
        delegate.setTemporary(temporary);
    }

    @Deprecated
    public String getDevice() {
        return delegate.getDevice();
    }

    public void setSecretData(String secretData) {
        delegate.setSecretData(secretData);
    }

    public String getCredentialData() {
        return delegate.getCredentialData();
    }

    @Deprecated
    public String getHashedSaltedValue() {
        return delegate.getHashedSaltedValue();
    }

    @Deprecated
    public String getSalt() {
        return delegate.getSalt();
    }

    public void setCredentialData(String credentialData) {
        delegate.setCredentialData(credentialData);
    }

    public Integer getPriority() {
        return delegate.getPriority();
    }

    @Deprecated
    public Integer getHashIterations() {
        return delegate.getHashIterations();
    }

    @Deprecated
    public Integer getCounter() {
        return delegate.getCounter();
    }

    public void setPriority(Integer priority) {
        delegate.setPriority(priority);
    }

    public Long getCreatedDate() {
        return delegate.getCreatedDate();
    }

    @Deprecated
    public String getAlgorithm() {
        return delegate.getAlgorithm();
    }

    @Deprecated
    public Integer getDigits() {
        return delegate.getDigits();
    }

    public void setCreatedDate(Long createdDate) {
        delegate.setCreatedDate(createdDate);
    }

    public String getValue() {
        return delegate.getValue();
    }

    @Deprecated
    public Integer getPeriod() {
        return delegate.getPeriod();
    }

    @Deprecated
    public MultiAttributeMap getConfig(PagingOptions options) {
        return new MultiAttributeMap(delegate.getConfig(), options);
    }

    public void setValue(String value) {
        delegate.setValue(value);
    }

    public Boolean isTemporary() {
        return delegate.isTemporary();
    }

    public String getSecretData() {
        return delegate.getSecretData();
    }

    @Override
    @GraphQLIgnore
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    @GraphQLIgnore
    public boolean equals(Object obj) {
        if (obj instanceof CredentialType) {
            return delegate.equals(((CredentialType)obj).getCredentialRepresentation());
        }

        return false;
    }
}
