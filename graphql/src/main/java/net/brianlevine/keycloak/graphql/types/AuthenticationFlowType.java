package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;

import java.io.Serializable;
import java.util.List;

@GraphQLType
@SuppressWarnings("unused")
public class AuthenticationFlowType implements Serializable {
    private final AuthenticationFlowRepresentation delegate;

    public AuthenticationFlowType(AuthenticationFlowRepresentation authenticationFlowRepresentation) {
        this.delegate = authenticationFlowRepresentation;
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

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public String getProviderId() {
        return delegate.getProviderId();
    }

    public void setProviderId(String providerId) {
        delegate.setProviderId(providerId);
    }

    public boolean isTopLevel() {
        return delegate.isTopLevel();
    }

    public void setTopLevel(boolean topLevel) {
        delegate.setTopLevel(topLevel);
    }

    public boolean isBuiltIn() {
        return delegate.isBuiltIn();
    }

    public void setBuiltIn(boolean builtIn) {
        delegate.setBuiltIn(builtIn);
    }

    public List<AuthenticationExecutionExportRepresentation> getAuthenticationExecutions() {
        return delegate.getAuthenticationExecutions();
    }

    public Page<AuthenticationExecutionExportType> getAuthenticationExecutions(PagingOptions options) {
        return Page.toPagedType(
                options,
                AuthenticationExecutionExportType.class,
                AuthenticationExecutionExportRepresentation.class,
                delegate::getAuthenticationExecutions);
    }

    public void setAuthenticationExecutions(List<AuthenticationExecutionExportRepresentation> authenticationExecutions) {
        delegate.setAuthenticationExecutions(authenticationExecutions);
    }
}
