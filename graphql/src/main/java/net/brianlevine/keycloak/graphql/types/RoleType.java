package net.brianlevine.keycloak.graphql.types;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import jakarta.persistence.EntityManager;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@GraphQLType
@SuppressWarnings("unused")
public class RoleType implements BaseType {
    private final RoleRepresentation delegate;

    @SuppressWarnings("FieldCanBeLocal")
    private final KeycloakSession kcSession;

    @SuppressWarnings("FieldCanBeLocal")
    private final RealmModel realm;

    @SuppressWarnings("FieldCanBeLocal")
    private RoleModel roleModel;

    public RoleType(KeycloakSession kcSession, RealmModel realm, RoleRepresentation delegate) {
        this.kcSession = kcSession;
        this.delegate = delegate;
        this.realm = realm;
        this.roleModel = null;
    }

    public RoleType(KeycloakSession kcSession, RealmModel realm, RoleModel roleModel) {
        this.kcSession = kcSession;
        this.realm = realm;
        this.delegate = ModelToRepresentation.toRepresentation(roleModel);
        this.roleModel = roleModel;
    }

    @GraphQLIgnore
    public RoleModel getRoleModel() {
        if (roleModel == null) {
            roleModel = getKeycloakSession().roles().getRoleById(getRealmModel(), getId());
        }

        return roleModel;
    }

    @Override
    @GraphQLIgnore
    public RealmModel getRealmModel() {
        return realm;
    }

    @Override
    @GraphQLIgnore
    public KeycloakSession getKeycloakSession() {
        return kcSession;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
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

    @GraphQLQuery(name = "compositeRoles")
    public Page<RoleType> getComposites(PagingOptions options) {
        JpaConnectionProvider connection = kcSession.getProvider(JpaConnectionProvider.class);
        EntityManager em = connection.getEntityManager();

        //TODO: Performance: Maybe use SQL query instead of stream.count(). Many other places in the code
        //      where we might want to do this. Maybe move the EntityManager setup (above) to a utility function
        //      assuming we'll need that in multiple types.

//        Query q = em.createNativeQuery("select count(*) from composite_role where composite = :id");
//        q.setParameter("id", getId());
//
//        long count = (long)q.getSingleResult();

        options = options == null ? new PagingOptions() : options;
        long count = getRoleModel().getCompositesStream().count();
        Stream<RoleModel> composites = getRoleModel().getCompositesStream(null, options.start, options.limit);
        List<RoleType> items = composites.map(c -> new RoleType(kcSession, realm, c)).toList();

        return new Page<>((int)count, options.limit, items);
    }


    public boolean isComposite() {
        return delegate.isComposite();
    }

    public void setComposite(boolean composite) {
        delegate.setComposite(composite);
    }

    public boolean isClientRole() {
        return delegate.getClientRole();
    }

    public void setClientRole(boolean clientRole) {
        delegate.setClientRole(clientRole);
    }

    public String getContainerId() {
        return delegate.getContainerId();
    }

    @GraphQLQuery
    public Container getContainer() {
        Container ret = null;

        if (isClientRole()) {
            ClientModel clientModel = kcSession.clients().getClientById(realm, getContainerId());

            if (clientModel != null) {
                ret = new ClientType(kcSession, realm, clientModel);
            }

        } else {
            RealmModel realmModel = kcSession.realms().getRealm(getContainerId());
            ret = new RealmType(kcSession, realmModel);
        }

        return ret;
    }

    public void setContainerId(String containerId) {
        delegate.setContainerId(containerId);
    }

    public MultiAttributeMap getAttributes(PagingOptions options) {
        return new MultiAttributeMap(delegate.getAttributes(), options);
    }

//    public void setAttributes(Map<String, List<String>> attributes) {
//        delegate.setAttributes(attributes);
//    }
}