package net.brianlevine.keycloak.graphql.types;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLIgnore;

import net.brianlevine.keycloak.graphql.util.Auth;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.permissions.GroupPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class UserType implements RoleHolder, GroupHolder, BaseType {
    private final UserRepresentation delegate;

    @SuppressWarnings("FieldCanBeLocal")
    private final KeycloakSession kcSession;

    @SuppressWarnings("FieldCanBeLocal")
    private final RealmModel realmModel;

    @SuppressWarnings("FieldCanBeLocal")
    private UserModel userModel;

    public UserType(KeycloakSession kcSession, RealmModel realmModel, UserRepresentation delegate) {
        this.kcSession = kcSession;
        this.delegate = delegate;
        this.realmModel = realmModel;
        this.userModel = null;
    }

    public UserType(KeycloakSession kcSession, RealmModel realmModel, UserModel userModel) {
        this.kcSession = kcSession;
        this.realmModel = realmModel;
        this.userModel = userModel;
        this.delegate = ModelToRepresentation.toRepresentation(kcSession, realmModel, userModel);
    }

    @GraphQLIgnore
    public UserModel getUserModel() {
        if (userModel == null) {
            userModel = kcSession.users().getUserById(realmModel, getId());
        }

        return userModel;
    }

    @Override
    @GraphQLIgnore
    public Stream<RoleModel> getRolesStream(GraphQLContext ctx) {
        UserPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).users();
        return eval.canView(getUserModel()) ? getUserModel().getRoleMappingsStream() : Stream.empty();
    }

    @Override
    @GraphQLIgnore
    public Stream<RoleModel> getRolesStream(int start, int limit, GraphQLContext ctx) {
        UserPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).users();
        return eval.canView(getUserModel()) ? getUserModel().getRoleMappingsStream().skip(start).limit(limit) : Stream.empty();
    }

    @Override
    @GraphQLIgnore
    public Stream<GroupModel> getGroupsStream(int start, int limit, GraphQLContext ctx) {
        Stream<GroupModel> stream = getUserModel().getGroupsStream(null, start, limit);

        GroupPermissionEvaluator groupsEvaluator = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).groups();

        boolean canViewGlobal = groupsEvaluator.canView();
        return stream.filter(g -> canViewGlobal || groupsEvaluator.canView(g));
    }

    @Override
    @GraphQLIgnore
    public Stream<GroupModel> getGroupsStream(GraphQLContext ctx) {
        Stream<GroupModel> stream = getUserModel().getGroupsStream();

        GroupPermissionEvaluator groupsEvaluator = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).groups();

        boolean canViewGlobal = groupsEvaluator.canView();
        return stream.filter(g -> canViewGlobal || groupsEvaluator.canView(g));
    }

    @GraphQLIgnore
    public RealmModel getRealmModel() {
        return realmModel;
    }

    @GraphQLIgnore
    public KeycloakSession getKeycloakSession() {
        return kcSession;
    }


    public String getId() {
        return delegate.getId();
    }

    public int getNotBefore() {
        return delegate.getNotBefore();
    }


    public String getUsername() {
        return delegate.getUsername();
    }


    public void setUsername(String username) {
        delegate.setUsername(username);
    }


    public boolean isEnabled() {
        return delegate.isEnabled();
    }


    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }



    public MultiAttributeMap getAttributes(PagingOptions options) {
        return new MultiAttributeMap(delegate.getAttributes(), options);
    }


    public List<String> getRequiredActions() {
        return delegate.getRequiredActions();
    }



    public String getFirstName() {
        return delegate.getFirstName();
    }


    public void setFirstName(String firstName) {
        delegate.setFirstName(firstName);
    }


    public String getLastName() {
        return delegate.getLastName();
    }


    public void setLastName(String lastName) {
        delegate.setLastName(lastName);
    }


    public String getEmail() {
        return delegate.getEmail();
    }


    public void setEmail(String email) {
        delegate.setEmail(email);
    }


    public boolean isEmailVerified() {
        return delegate.isEmailVerified();
    }


    public void setEmailVerified(boolean verified) {
        delegate.setEmailVerified(verified);
    }

    public String getServiceAccountClientId() {
        return delegate.getServiceAccountClientId();
    }



    public String getFederationLink() {
        return delegate.getFederationLink();
    }


    public void setFederationLink(String link) {
        delegate.setFederationLink(link);
    }


    public Date getCreatedTimestamp(){
        Date date = new Date(delegate.getCreatedTimestamp());
        return date;
    }


    public void setCreatedTimestamp(Date timestamp){
        delegate.setCreatedTimestamp(timestamp.getTime());
    }


//    public void joinGroup(GroupModel group) {
//        delegate.joinGroup(group);
//
//    }


//    public void leaveGroup(GroupModel group) {
//        delegate.leaveGroup(group);
//
//    }


    //public boolean isMemberOf(GroupModel group) {
    //    return delegate.isMemberOf(group);
    //}

}
