package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.util.Page;
import net.brianlevine.keycloak.graphql.util.PagedMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.OrganizationDomainRepresentation;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import java.util.List;
import java.util.Set;

@GraphQLType
@SuppressWarnings("unused")
public class OrganizationType {
    private final OrganizationRepresentation delegate;
    private final KeycloakSession kcSession;

    public OrganizationType(KeycloakSession kcSession, OrganizationRepresentation organizationRepresentation) {
        this.delegate = organizationRepresentation;
        this.kcSession = kcSession;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getName() {
        return delegate.getName();
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @GraphQLQuery
    public MultiAttributeMap getAttributes(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        return new MultiAttributeMap(delegate.getAttributes(), start, limit);
    }
//
//    public void setAttributes(Map<String, List<String>> attributes) {
//        delegate.setAttributes(attributes);
//    }
//
//    public OrganizationRepresentation singleAttribute(String name, String value) {
//        if (delegate.getAttributes() == null)
//            delegate.setAttributes(new HashMap<String, List<String>>());
//        delegate.getAttributes().put(name, Arrays.asList(value));
//        return delegate;
//    }

    public Page<OrganizationDomainType> getDomains(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        Set<OrganizationDomainRepresentation> domains =  delegate.getDomains();
        return new Page<>(domains.size(), limit, domains.stream().skip(start).limit(limit).map(OrganizationDomainType::new).toList());
    }

    public OrganizationDomainType getDomain(String name) {
        OrganizationDomainRepresentation rep = delegate.getDomain(name);
        return rep != null ? new OrganizationDomainType(rep) : null;
    }

//    public void addDomain(OrganizationDomainRepresentation domain) {
//        delegate.addDomain(domain);
//    }
//
//    public void removeDomain(OrganizationDomainRepresentation domain) {
//        if (delegate.getDomains() == null) {
//            return;
//        }
//        getDomains().remove(domain);
//    }

    public Page<UserType> getMembers(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<UserRepresentation> members =  delegate.getMembers();
        List<UserType> users = members.stream()
                .skip(start)
                .limit(limit)
                .map(u -> new UserType(kcSession, kcSession.getContext().getRealm(), u))
                .toList();

        return new Page<>(members.size(), limit, users);
    }

//    public void setMembers(List<UserRepresentation> members) {
//        delegate.setMembers(members);
//    }
//
//    public void addMember(UserRepresentation user) {
//        if (delegate.getMembers() == null) {
//            delegate.setMembers(new ArrayList<UserRepresentation>());
//        }
//        delegate.getMembers().add(user);
//    }

    public Page<IdentityProviderType> getIdentityProviders(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<IdentityProviderRepresentation> identityProviders =  delegate.getIdentityProviders();
        List<IdentityProviderType> idps = identityProviders.stream()
                .skip(start)
                .limit(limit)
                .map(IdentityProviderType::new)
                .toList();

        return new Page<>(identityProviders.size(), limit, idps);
    }

//    public void setIdentityProviders(List<IdentityProviderRepresentation> identityProviders) {
//        delegate.setIdentityProviders(identityProviders);
//    }
//
//    public void addIdentityProvider(IdentityProviderRepresentation idp) {
//        if (delegate.getIdentityProviders() == null) {
//            delegate.setIdentityProviders(new ArrayList<IdentityProviderRepresentation>());
//        }
//        delegate.getIdentityProviders().add(idp);
//    }
}
