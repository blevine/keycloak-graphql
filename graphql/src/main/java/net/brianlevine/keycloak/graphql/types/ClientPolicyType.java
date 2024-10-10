package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.representations.idm.ClientPolicyConditionRepresentation;
import org.keycloak.representations.idm.ClientPolicyRepresentation;

import java.util.List;

@GraphQLType
@SuppressWarnings("unused")
public class ClientPolicyType {
    private final ClientPolicyRepresentation delegate;

    public ClientPolicyType(ClientPolicyRepresentation clientPolicyRepresentation) {
        this.delegate = clientPolicyRepresentation;
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public Boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }

    @GraphQLQuery
    public Page<ClientPolicyConditionType> getConditions(@GraphQLArgument PagingOptions options) {
        List<ClientPolicyConditionRepresentation> reps = delegate.getConditions();
        List<ClientPolicyConditionType> conditions = reps.stream()
                .skip(options.start)
                .limit(options.limit)
                .map(ClientPolicyConditionType::new)
                .toList();

        return new Page<>(reps.size(), options.limit, conditions);
    }

    public void setConditions(List<ClientPolicyConditionRepresentation> conditions) {
        delegate.setConditions(conditions);
    }

    public List<String> getProfiles() {
        return delegate.getProfiles();
    }

    public void setProfiles(List<String> profiles) {
        delegate.setProfiles(profiles);
    }

    @SuppressWarnings("com.intellij.jpb.inspection.EqualsDoesntCheckParameterClassInspection")
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
