package net.brianlevine.keycloak.graphql.types;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import io.leangen.graphql.annotations.types.GraphQLType;
import net.brianlevine.keycloak.graphql.util.Auth;
import net.brianlevine.keycloak.graphql.util.Page;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.services.resources.admin.permissions.GroupPermissionEvaluator;
import org.keycloak.utils.GroupUtils;

import java.util.*;
import java.util.stream.Stream;

@GraphQLType
@SuppressWarnings("unused")
public class GroupType implements RoleHolder, BaseType {
    private final GroupRepresentation delegate;

    @SuppressWarnings("FieldCanBeLocal")
    private final KeycloakSession kcSession;

    @SuppressWarnings("FieldCanBeLocal")
    private final RealmModel realm;

    @SuppressWarnings("FieldCanBeLocal")
    private GroupModel groupModel;

    public GroupType(KeycloakSession kcSession, RealmModel realm, GroupRepresentation delegate) {
        this.delegate = delegate;
        this.kcSession = kcSession;
        this.realm = realm;
        this.groupModel = null;
    }

    public GroupType(KeycloakSession kcSession, RealmModel realm, GroupModel groupModel) {
        this.delegate = ModelToRepresentation.toRepresentation(groupModel, true);
        this.kcSession = kcSession;
        this.realm = realm;
        this.groupModel = groupModel;
    }

    @GraphQLIgnore
    public GroupModel getGroupModel() {
        if (groupModel == null) {
            groupModel = realm.getGroupById(getId());
        }

        return groupModel;
    }

    @Override
    @GraphQLIgnore
    public Stream<RoleModel> getRolesStream(GraphQLContext ctx) {
        GroupPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).groups();
        return eval.canView(getGroupModel()) ? getGroupModel().getRoleMappingsStream() : Stream.empty();
    }

    @Override
    @GraphQLIgnore
    public Stream<RoleModel> getRolesStream(int start, int limit, GraphQLContext ctx) {
        GroupPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).groups();
        return eval.canView(getGroupModel()) ? getGroupModel().getRoleMappingsStream().skip(start).limit(limit) : Stream.empty();
    }

    @GraphQLIgnore
    public RealmModel getRealmModel() {
        return realm;
    }

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

    public String getPath() {
        return delegate.getPath();
    }

    public void setPath(String path) {
        delegate.setPath(path);
    }

    public String getParentId() {
        return delegate.getParentId();
    }

    public void setParentId(String parentId) {
        delegate.setParentId(parentId);
    }

    //public Long getSubGroupCount() {
    //    return delegate.getSubGroupCount();
    //}

    //public void setSubGroupCount(Long subGroupCount) {
    //    delegate.setSubGroupCount(subGroupCount);
    //}


    //public void setRealmRoles(List<String> realmRoles) {
    //    delegate.setRealmRoles(realmRoles);
    //}

    //public void setClientRoles(Map<String, List<String>> clientRoles) {
    //    delegate.setClientRoles(clientRoles);
    //}


    public Map<String, List<String>> getAttributes() {
        return delegate.getAttributes();
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        delegate.setAttributes(attributes);
    }

//    public GroupRepresentation singleAttribute(String name, String value) {
//        if (delegate.getAttributes() == null)
//            delegate.setAttributes(new HashMap<String, List<String>>());
//        delegate.getAttributes().put(name, Arrays.asList(value));
//        return delegate;
//    }

    @GraphQLQuery
    public Page<GroupType> getSubGroups(
            @GraphQLArgument(defaultValue = "0")int start,
            @GraphQLArgument(defaultValue = "100")int limit,
            @GraphQLRootContext GraphQLContext ctx) {

        Page<GroupType> ret;

        GroupModel gm = getGroupModel();
        RealmModel realmModel = getRealmModel();
        KeycloakSession session = getKeycloakSession();
        GroupPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx.get("headers"), session, realmModel).groups();

        // TODO: Do we need to evaluate the permission on the parent group here? If the caller is able to get here, then
        //       didn't they already have access to the parent group?
        //
        if (eval.canView(getGroupModel())) {
            boolean canViewGlobal = eval.canView();

            long subGroupCount = gm.getSubGroupsStream().filter(g -> canViewGlobal || eval.canView(g)).count();
            Stream<GroupModel> groupModels = gm.getSubGroupsStream(start, limit).filter(g -> canViewGlobal || eval.canView(g));

            List<GroupType> groupTypes = groupModels.map(g -> new GroupType(session, realmModel, GroupUtils.toRepresentation(eval, g, true))).toList();

            ret = new Page<>((int)subGroupCount, limit, groupTypes);
        }
        else {
            ret = Page.emptyPage();
        }

        return ret;
    }

    //public void setSubGroups(List<GroupRepresentation> subGroups) {
    //    delegate.setSubGroups(subGroups);
    //}

    public Map<String, Boolean> getAccess() {
        return delegate.getAccess();
    }

    public void setAccess(Map<String, Boolean> access) {
        delegate.setAccess(access);
    }

    //public void merge(GroupRepresentation g) {
    //    merge(delegate, g);
    //}

//    private void merge(GroupRepresentation g1, GroupRepresentation g2) {
//        if (g1.equals(g2)) {
//            Map<String, GroupRepresentation> g1Children = g1.getSubGroups().stream().collect(Collectors.toMap(GroupRepresentation::getId, g -> g));
//            Map<String, GroupRepresentation> g2Children = g2.getSubGroups().stream().collect(Collectors.toMap(GroupRepresentation::getId, g -> g));
//
//            g2Children.forEach((key, value) -> {
//                if (g1Children.containsKey(key)) {
//                    merge(g1Children.get(key), value);
//                } else {
//                    g1Children.put(key, value);
//                }
//            });
//            g1.setSubGroups(new ArrayList<GroupRepresentation>(g1Children.values()));
//        }
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupType that = (GroupType) o;
        boolean isEqual = Objects.equals(getId(), that.getId()) && Objects.equals(getParentId(), that.getParentId());
        if(isEqual) {
            return true;
        } else {
            return Objects.equals(getName(), that.getName()) && Objects.equals(getPath(), that.getPath());
        }
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}

