package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.OrganizationDomainRepresentation;

@GraphQLType
@SuppressWarnings("unused")
public class OrganizationDomainType {
    private final OrganizationDomainRepresentation delegate;

    public OrganizationDomainType(OrganizationDomainRepresentation delegate) {
        this.delegate = delegate;
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public boolean isVerified() {
        return delegate.isVerified();
    }

    public void setVerified(boolean verified) {
        delegate.setVerified(verified);
    }
}
