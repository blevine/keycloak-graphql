package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.types.GraphQLType;
import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;

import java.io.Serializable;

@GraphQLType
@SuppressWarnings("unused")
public class AuthenticationExecutionExportType implements Serializable {
    private final AuthenticationExecutionExportRepresentation delegate;

    public AuthenticationExecutionExportType(AuthenticationExecutionExportRepresentation authenticationExecutionExportRepresentation) {
        this.delegate = authenticationExecutionExportRepresentation;
    }

    public boolean isUserSetupAllowed() {
        return delegate.isUserSetupAllowed();
    }

    public void setUserSetupAllowed(boolean userSetupAllowed) {
        delegate.setUserSetupAllowed(userSetupAllowed);
    }

    public String getFlowAlias() {
        return delegate.getFlowAlias();
    }

    public void setFlowAlias(String flowId) {
        delegate.setFlowAlias(flowId);
    }
}
