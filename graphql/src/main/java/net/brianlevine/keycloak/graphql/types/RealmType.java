package net.brianlevine.keycloak.graphql.types;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import io.leangen.graphql.annotations.types.GraphQLType;

import net.brianlevine.keycloak.graphql.util.Auth;
import net.brianlevine.keycloak.graphql.util.Page;
import net.brianlevine.keycloak.graphql.util.Util;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientProfileRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.representations.idm.ComponentExportRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.GroupResource;
import org.keycloak.services.resources.admin.UserResource;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.GroupPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.RolePermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@GraphQLType
@SuppressWarnings("unused")
public class RealmType implements Container, GroupHolder, RoleHolder, BaseType {

    private final RealmRepresentation delegate;
    private final KeycloakSession kcSession;
    private RealmModel realmModel;

    public RealmType(KeycloakSession kcSession, RealmRepresentation delegate) {
        this.delegate = delegate;
        this.kcSession = kcSession;
    }

    public RealmType(KeycloakSession kcSession, RealmModel realmModel) {
        this(kcSession, ModelToRepresentation.toRepresentation(kcSession, realmModel, false));
        this.realmModel = realmModel;
    }

    @Override
    @GraphQLIgnore
    public Stream<RoleModel> getRolesStream(GraphQLContext ctx) {
        RolePermissionEvaluator evaluator = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).roles();

        return evaluator.canList(getRealmModel()) ? getRealmModel().getRolesStream() : Stream.empty();
    }

    @Override
    @GraphQLIgnore
    public Stream<RoleModel> getRolesStream(int start, int limit, GraphQLContext ctx) {
        RolePermissionEvaluator evaluator = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).roles();

        return evaluator.canList(getRealmModel()) ? getRealmModel().getRolesStream(start, limit) : Stream.empty();
    }

    @Override
    @GraphQLIgnore
    public Stream<GroupModel> getGroupsStream(int start, int limit, GraphQLContext ctx) {
        Stream<GroupModel> stream =  kcSession.groups().getTopLevelGroupsStream(getRealmModel(), start, limit);

        GroupPermissionEvaluator groupsEvaluator = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).groups();

        boolean canViewGlobal = groupsEvaluator.canView();
        return stream.filter(g -> canViewGlobal || groupsEvaluator.canView(g));
    }

    @Override
    @GraphQLIgnore
    public Stream<GroupModel> getGroupsStream(GraphQLContext ctx) {
        Stream<GroupModel> stream =  kcSession.groups().getTopLevelGroupsStream(getRealmModel());

        GroupPermissionEvaluator groupsEvaluator = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel()).groups();

        boolean canViewGlobal = groupsEvaluator.canView();
        return stream.filter(g -> canViewGlobal || groupsEvaluator.canView(g));
    }


    @GraphQLIgnore
    public RealmModel getRealmModel() {
        if (realmModel == null) {
            realmModel = kcSession.realms().getRealm(getId());
        }

        return realmModel;
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


    public String getRealm() {
        return delegate.getRealm();
    }


    public void setRealm(String realm) {
        delegate.setRealm(realm);
    }

    public String getName() {
        return getRealm();
    }


    public String getDisplayName() {
        return delegate.getDisplayName();
    }


    public void setDisplayName(String displayName) {
        delegate.setDisplayName(displayName);
    }


    public String getDisplayNameHtml() {
        return delegate.getDisplayNameHtml();
    }


    public void setDisplayNameHtml(String displayNameHtml) {
        delegate.setDisplayNameHtml(displayNameHtml);
    }

    @GraphQLQuery
    //public Page<UserType> getUsers(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit, @GraphQLRootContext GraphQLContext ctx) {
    public Page<UserType> getUsers(@GraphQLArgument(name="args") StdCollectionArgs collectionArgs,  @GraphQLRootContext GraphQLContext ctx) {
        RealmModel realm = kcSession.realms().getRealm(getId());
        UserPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx, getRealmModel()).users();

        Stream<UserModel> userModels = kcSession.users().searchForUserStream(realm, Collections.emptyMap(), collectionArgs.start, collectionArgs.limit);
        Stream<UserRepresentation> userReps =  RealmType.toUserRepresentation(kcSession, realm, eval, userModels);
        List<UserType> userTypes = userReps.map(u -> new UserType(kcSession, realm, u)).toList();

        int userCount = userTypes.size();
        Page<UserType> page = new Page<>(userCount, collectionArgs.limit, userTypes);

        return page;
    }

    @GraphQLQuery(name="user")
    public UserType getUserById(@GraphQLArgument(name = "id") String id, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = getKeycloakSession();
        RealmModel realmModel = getRealmModel();
        UserModel userModel = session.users().getUserById(realmModel, id);
        return toUserType(ctx, session, realmModel, userModel);
    }

    @GraphQLQuery(name="user")
    public UserType getUserByUsername(@GraphQLArgument(name = "username") String username, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = getKeycloakSession();
        RealmModel realmModel = getRealmModel();
        UserModel userModel = session.users().getUserByUsername(realmModel, username);
        return toUserType(ctx, session, realmModel, userModel);
    }

    @GraphQLQuery(name="user")
    public UserType getUserByEmail(@GraphQLArgument(name = "email") String email, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = getKeycloakSession();
        RealmModel realmModel = getRealmModel();
        UserModel userModel = session.users().getUserByEmail(realmModel, email);
        return toUserType(ctx, session, realmModel, userModel);
    }


    public UserType toUserType(GraphQLContext ctx, KeycloakSession session, RealmModel realmModel, UserModel userModel) {
        UserType ret = null;

        if (userModel != null) {
            AdminPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx, getRealmModel());

            // HACK:  because UserResource sets realm to session.getContext().getRealm() in it's constructor.
            RealmModel sessionRealm = session.getContext().getRealm();
            session.getContext().setRealm(realmModel);

            try {
                UserResource ur = new UserResource(
                        session,
                        userModel,
                        eval,
                        Util.createAdminEventBuilder(session, realmModel, eval.adminAuth()));

                if (eval.users().canView(userModel)) {
                    UserRepresentation userRep = ur.getUser(true);
                    ret = new UserType(session, realmModel, userRep);
                }
            } finally {
                session.getContext().setRealm(sessionRealm);
            }
        }

        return ret;
    }


    @GraphQLQuery(name="group")
    public GroupType getGroupById(@GraphQLArgument(name = "id") String id, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = getKeycloakSession();
        RealmModel realmModel = getRealmModel();
        GroupModel groupModel = session.groups().getGroupById(realmModel, id);
        return toGroupType(ctx, session, realmModel, groupModel);
    }

    @GraphQLQuery(name="group")
    public GroupType getGroupByPath(@GraphQLArgument(name = "path") String path, @GraphQLRootContext GraphQLContext ctx) {
        KeycloakSession session = getKeycloakSession();
        RealmModel realmModel = getRealmModel();
        GroupModel groupModel = KeycloakModelUtils.findGroupByPath(session, realmModel, path);
        return toGroupType(ctx, session, realmModel, groupModel);
    }

    public GroupType toGroupType(GraphQLContext ctx, KeycloakSession session, RealmModel realmModel, GroupModel groupModel) {
        GroupType ret = null;

        if (groupModel != null) {
            AdminPermissionEvaluator eval = Auth.getAdminPermissionEvaluator(ctx, getRealmModel());
            AdminEventBuilder adminEventBuilder = Util.createAdminEventBuilder(session, realmModel, eval.adminAuth());

            GroupResource gr = new GroupResource(
                    realmModel,
                    groupModel,
                    session,
                    eval,
                    adminEventBuilder);

            if (eval.groups().canView(groupModel)) {
                GroupRepresentation groupRep = gr.getGroup();
                ret = new GroupType(session, realmModel, groupRep);
            }
        }

        return ret;
    }



    public static Stream<UserRepresentation> toUserRepresentation(KeycloakSession session, RealmModel realm, UserPermissionEvaluator usersEvaluator, Stream<UserModel> userModels) {

        boolean canViewGlobal = usersEvaluator.canView();

        usersEvaluator.grantIfNoPermission(session.getAttribute(UserModel.GROUPS) != null);
        return userModels.filter(user -> canViewGlobal || usersEvaluator.canView(user))
                .map(user -> {
                    UserRepresentation userRep = ModelToRepresentation.toRepresentation(session, realm, user);
                    userRep.setAccess(usersEvaluator.getAccess(user));
                    return userRep;
                });
    }

    @GraphQLQuery
    public Page<ClientType> getClients(@GraphQLArgument(defaultValue = "0") int start, @GraphQLArgument(defaultValue = "100") int limit) {
        Stream<ClientModel> clientModels = getRealmModel().getClientsStream(start, limit);
        List<ClientType> clients = clientModels.map(c -> new ClientType(getKeycloakSession(), getRealmModel(), c)).toList();
        long count = getRealmModel().getClientsCount();

        return new Page<>((int)count, limit, clients);
    }


    public Boolean isEnabled() {
        return delegate.isEnabled();
    }


    public void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }


    public String getSslRequired() {
        return delegate.getSslRequired();
    }


    public void setSslRequired(String sslRequired) {
        delegate.setSslRequired(sslRequired);
    }


    public String getDefaultSignatureAlgorithm() {
        return delegate.getDefaultSignatureAlgorithm();
    }


    public void setDefaultSignatureAlgorithm(String defaultSignatureAlgorithm) {
        delegate.setDefaultSignatureAlgorithm(defaultSignatureAlgorithm);
    }


    public Boolean getRevokeRefreshToken() {
        return delegate.getRevokeRefreshToken();
    }


    public void setRevokeRefreshToken(Boolean revokeRefreshToken) {
        delegate.setRevokeRefreshToken(revokeRefreshToken);
    }


    public Integer getRefreshTokenMaxReuse() {
        return delegate.getRefreshTokenMaxReuse();
    }


    public void setRefreshTokenMaxReuse(Integer refreshTokenMaxReuse) {
        delegate.setRefreshTokenMaxReuse(refreshTokenMaxReuse);
    }


    public Integer getAccessTokenLifespan() {
        return delegate.getAccessTokenLifespan();
    }


    public void setAccessTokenLifespan(Integer accessTokenLifespan) {
        delegate.setAccessTokenLifespan(accessTokenLifespan);
    }


    public Integer getAccessTokenLifespanForImplicitFlow() {
        return delegate.getAccessTokenLifespanForImplicitFlow();
    }


    public void setAccessTokenLifespanForImplicitFlow(Integer accessTokenLifespanForImplicitFlow) {
        delegate.setAccessTokenLifespanForImplicitFlow(accessTokenLifespanForImplicitFlow);
    }


    public Integer getSsoSessionIdleTimeout() {
        return delegate.getSsoSessionIdleTimeout();
    }


    public void setSsoSessionIdleTimeout(Integer ssoSessionIdleTimeout) {
        delegate.setSsoSessionIdleTimeout(ssoSessionIdleTimeout);
    }


    public Integer getSsoSessionMaxLifespan() {
        return delegate.getSsoSessionMaxLifespan();
    }


    public void setSsoSessionMaxLifespan(Integer ssoSessionMaxLifespan) {
        delegate.setSsoSessionMaxLifespan(ssoSessionMaxLifespan);
    }


    public Integer getSsoSessionMaxLifespanRememberMe() {
        return delegate.getSsoSessionMaxLifespanRememberMe();
    }


    public void setSsoSessionMaxLifespanRememberMe(Integer ssoSessionMaxLifespanRememberMe) {
        delegate.setSsoSessionMaxLifespanRememberMe(ssoSessionMaxLifespanRememberMe);
    }


    public Integer getSsoSessionIdleTimeoutRememberMe() {
        return delegate.getSsoSessionIdleTimeoutRememberMe();
    }


    public void setSsoSessionIdleTimeoutRememberMe(Integer ssoSessionIdleTimeoutRememberMe) {
        delegate.setSsoSessionIdleTimeoutRememberMe(ssoSessionIdleTimeoutRememberMe);
    }


    public Integer getOfflineSessionIdleTimeout() {
        return delegate.getOfflineSessionIdleTimeout();
    }


    public void setOfflineSessionIdleTimeout(Integer offlineSessionIdleTimeout) {
        delegate.setOfflineSessionIdleTimeout(offlineSessionIdleTimeout);
    }


    public Boolean getOfflineSessionMaxLifespanEnabled() {
        return delegate.getOfflineSessionMaxLifespanEnabled();
    }


    public void setOfflineSessionMaxLifespanEnabled(Boolean offlineSessionMaxLifespanEnabled) {
        delegate.setOfflineSessionMaxLifespanEnabled(offlineSessionMaxLifespanEnabled);
    }


    public Integer getOfflineSessionMaxLifespan() {
        return delegate.getOfflineSessionMaxLifespan();
    }


    public void setOfflineSessionMaxLifespan(Integer offlineSessionMaxLifespan) {
        delegate.setOfflineSessionMaxLifespan(offlineSessionMaxLifespan);
    }


    public Integer getClientSessionIdleTimeout() {
        return delegate.getClientSessionIdleTimeout();
    }


    public void setClientSessionIdleTimeout(Integer clientSessionIdleTimeout) {
        delegate.setClientSessionIdleTimeout(clientSessionIdleTimeout);
    }


    public Integer getClientSessionMaxLifespan() {
        return delegate.getClientSessionMaxLifespan();
    }


    public void setClientSessionMaxLifespan(Integer clientSessionMaxLifespan) {
        delegate.setClientSessionMaxLifespan(clientSessionMaxLifespan);
    }


    public Integer getClientOfflineSessionIdleTimeout() {
        return delegate.getClientOfflineSessionIdleTimeout();
    }


    public void setClientOfflineSessionIdleTimeout(Integer clientOfflineSessionIdleTimeout) {
        delegate.setClientOfflineSessionIdleTimeout(clientOfflineSessionIdleTimeout);
    }


    public Integer getClientOfflineSessionMaxLifespan() {
        return delegate.getClientOfflineSessionMaxLifespan();
    }


    public void setClientOfflineSessionMaxLifespan(Integer clientOfflineSessionMaxLifespan) {
        delegate.setClientOfflineSessionMaxLifespan(clientOfflineSessionMaxLifespan);
    }


    public String getPasswordPolicy() {
        return delegate.getPasswordPolicy();
    }


    public void setPasswordPolicy(String passwordPolicy) {
        delegate.setPasswordPolicy(passwordPolicy);
    }


    public Integer getAccessCodeLifespan() {
        return delegate.getAccessCodeLifespan();
    }


    public void setAccessCodeLifespan(Integer accessCodeLifespan) {
        delegate.setAccessCodeLifespan(accessCodeLifespan);
    }


    public Integer getAccessCodeLifespanUserAction() {
        return delegate.getAccessCodeLifespanUserAction();
    }


    public void setAccessCodeLifespanUserAction(Integer accessCodeLifespanUserAction) {
        delegate.setAccessCodeLifespanUserAction(accessCodeLifespanUserAction);
    }


    public Integer getAccessCodeLifespanLogin() {
        return delegate.getAccessCodeLifespanLogin();
    }


    public void setAccessCodeLifespanLogin(Integer accessCodeLifespanLogin) {
        delegate.setAccessCodeLifespanLogin(accessCodeLifespanLogin);
    }


    public Integer getActionTokenGeneratedByAdminLifespan() {
        return delegate.getActionTokenGeneratedByAdminLifespan();
    }


    public void setActionTokenGeneratedByAdminLifespan(Integer actionTokenGeneratedByAdminLifespan) {
        delegate.setActionTokenGeneratedByAdminLifespan(actionTokenGeneratedByAdminLifespan);
    }


    public void setOAuth2DeviceCodeLifespan(Integer oauth2DeviceCodeLifespan) {
        delegate.setOAuth2DeviceCodeLifespan(oauth2DeviceCodeLifespan);
    }


    public Integer getOAuth2DeviceCodeLifespan() {
        return delegate.getOAuth2DeviceCodeLifespan();
    }


    public void setOAuth2DevicePollingInterval(Integer oauth2DevicePollingInterval) {
        delegate.setOAuth2DevicePollingInterval(oauth2DevicePollingInterval);
    }


    public Integer getOAuth2DevicePollingInterval() {
        return delegate.getOAuth2DevicePollingInterval();
    }


    public Integer getActionTokenGeneratedByUserLifespan() {
        return delegate.getActionTokenGeneratedByUserLifespan();
    }


    public void setActionTokenGeneratedByUserLifespan(Integer actionTokenGeneratedByUserLifespan) {
        delegate.setActionTokenGeneratedByUserLifespan(actionTokenGeneratedByUserLifespan);
    }

    public RoleType getDefaultRole() {
        RoleRepresentation role = delegate.getDefaultRole();
        return new RoleType(getKeycloakSession(), getRealmModel(), role);
    }

    // TODO: Optimize with SQL query rather than iterating over groups by name
    @GraphQLQuery
    public Page<GroupType> getDefaultGroups(@GraphQLArgument(defaultValue = "0") int start, @GraphQLArgument(defaultValue = "100") int limit, @GraphQLRootContext GraphQLContext ctx) {
        AdminPermissionEvaluator auth = Auth.getAdminPermissionEvaluator(ctx.get("headers"), getKeycloakSession(), getRealmModel());

        if (auth.realm().canViewRealm()) {
            KeycloakSession session = getKeycloakSession();
            RealmModel realm = getRealmModel();

            Stream<GroupModel> groups = realm.getDefaultGroupsStream();
            long count = groups.count();

            groups = realm.getDefaultGroupsStream().skip(start).limit(limit);
            List<GroupType> groupTypes = groups.map(g -> new GroupType(session, realm, g)).toList();

            return new Page<>((int)count, limit, groupTypes);
        }
        else {
            return Page.emptyPage();
        }
    }

    public String getPrivateKey() {
        return delegate.getPrivateKey();
    }


    public void setPrivateKey(String privateKey) {
        delegate.setPrivateKey(privateKey);
    }


    public String getPublicKey() {
        return delegate.getPublicKey();
    }


    public void setPublicKey(String publicKey) {
        delegate.setPublicKey(publicKey);
    }


    public String getCertificate() {
        return delegate.getCertificate();
    }


    public void setCertificate(String certificate) {
        delegate.setCertificate(certificate);
    }


    public String getCodeSecret() {
        return delegate.getCodeSecret();
    }


    public void setCodeSecret(String codeSecret) {
        delegate.setCodeSecret(codeSecret);
    }


    public Boolean isPasswordCredentialGrantAllowed() {
        return delegate.isPasswordCredentialGrantAllowed();
    }


    public Boolean isRegistrationAllowed() {
        return delegate.isRegistrationAllowed();
    }


    public void setRegistrationAllowed(Boolean registrationAllowed) {
        delegate.setRegistrationAllowed(registrationAllowed);
    }


    public Boolean isRegistrationEmailAsUsername() {
        return delegate.isRegistrationEmailAsUsername();
    }


    public void setRegistrationEmailAsUsername(Boolean registrationEmailAsUsername) {
        delegate.setRegistrationEmailAsUsername(registrationEmailAsUsername);
    }


    public Boolean isRememberMe() {
        return delegate.isRememberMe();
    }


    public void setRememberMe(Boolean rememberMe) {
        delegate.setRememberMe(rememberMe);
    }


    public Boolean isVerifyEmail() {
        return delegate.isVerifyEmail();
    }


    public void setVerifyEmail(Boolean verifyEmail) {
        delegate.setVerifyEmail(verifyEmail);
    }


    public Boolean isLoginWithEmailAllowed() {
        return delegate.isLoginWithEmailAllowed();
    }


    public Boolean isDuplicateEmailsAllowed() {
        return delegate.isDuplicateEmailsAllowed();
    }


    public Boolean isResetPasswordAllowed() {
        return delegate.isResetPasswordAllowed();
    }


    public Boolean isEditUsernameAllowed() {
        return delegate.isEditUsernameAllowed();
    }


    public Map<String, String> getBrowserSecurityHeaders() {
        return delegate.getBrowserSecurityHeaders();
    }


    public Map<String, String> getSmtpServer() {
        return delegate.getSmtpServer();
    }


    public String getLoginTheme() {
        return delegate.getLoginTheme();
    }


    public void setLoginTheme(String loginTheme) {
        delegate.setLoginTheme(loginTheme);
    }


    public String getAccountTheme() {
        return delegate.getAccountTheme();
    }


    public void setAccountTheme(String accountTheme) {
        delegate.setAccountTheme(accountTheme);
    }


    public String getAdminTheme() {
        return delegate.getAdminTheme();
    }


    public void setAdminTheme(String adminTheme) {
        delegate.setAdminTheme(adminTheme);
    }


    public String getEmailTheme() {
        return delegate.getEmailTheme();
    }


    public void setEmailTheme(String emailTheme) {
        delegate.setEmailTheme(emailTheme);
    }


    public Integer getNotBefore() {
        return delegate.getNotBefore();
    }


    public void setNotBefore(Integer notBefore) {
        delegate.setNotBefore(notBefore);
    }


    public Boolean isBruteForceProtected() {
        return delegate.isBruteForceProtected();
    }


    public void setBruteForceProtected(Boolean bruteForceProtected) {
        delegate.setBruteForceProtected(bruteForceProtected);
    }


    public Boolean isPermanentLockout() {
        return delegate.isPermanentLockout();
    }


    public void setPermanentLockout(Boolean permanentLockout) {
        delegate.setPermanentLockout(permanentLockout);
    }


    public Integer getMaxTemporaryLockouts() {
        return delegate.getMaxTemporaryLockouts();
    }


    public void setMaxTemporaryLockouts(Integer maxTemporaryLockouts) {
        delegate.setMaxTemporaryLockouts(maxTemporaryLockouts);
    }


    public Integer getMaxFailureWaitSeconds() {
        return delegate.getMaxFailureWaitSeconds();
    }


    public void setMaxFailureWaitSeconds(Integer maxFailureWaitSeconds) {
        delegate.setMaxFailureWaitSeconds(maxFailureWaitSeconds);
    }


    public Integer getMinimumQuickLoginWaitSeconds() {
        return delegate.getMinimumQuickLoginWaitSeconds();
    }


    public void setMinimumQuickLoginWaitSeconds(Integer minimumQuickLoginWaitSeconds) {
        delegate.setMinimumQuickLoginWaitSeconds(minimumQuickLoginWaitSeconds);
    }


    public Integer getWaitIncrementSeconds() {
        return delegate.getWaitIncrementSeconds();
    }


    public void setWaitIncrementSeconds(Integer waitIncrementSeconds) {
        delegate.setWaitIncrementSeconds(waitIncrementSeconds);
    }


    public Long getQuickLoginCheckMilliSeconds() {
        return delegate.getQuickLoginCheckMilliSeconds();
    }


    public void setQuickLoginCheckMilliSeconds(Long quickLoginCheckMilliSeconds) {
        delegate.setQuickLoginCheckMilliSeconds(quickLoginCheckMilliSeconds);
    }


    public Integer getMaxDeltaTimeSeconds() {
        return delegate.getMaxDeltaTimeSeconds();
    }


    public void setMaxDeltaTimeSeconds(Integer maxDeltaTimeSeconds) {
        delegate.setMaxDeltaTimeSeconds(maxDeltaTimeSeconds);
    }


    public Integer getFailureFactor() {
        return delegate.getFailureFactor();
    }


    public void setFailureFactor(Integer failureFactor) {
        delegate.setFailureFactor(failureFactor);
    }


    public Boolean isEventsEnabled() {
        return delegate.isEventsEnabled();
    }


    public void setEventsEnabled(boolean eventsEnabled) {
        delegate.setEventsEnabled(eventsEnabled);
    }


    public Long getEventsExpiration() {
        return delegate.getEventsExpiration();
    }


    public void setEventsExpiration(long eventsExpiration) {
        delegate.setEventsExpiration(eventsExpiration);
    }


    public List<String> getEventsListeners() {
        return delegate.getEventsListeners();
    }


    public void setEventsListeners(List<String> eventsListeners) {
        delegate.setEventsListeners(eventsListeners);
    }


    public List<String> getEnabledEventTypes() {
        return delegate.getEnabledEventTypes();
    }


    public void setEnabledEventTypes(List<String> enabledEventTypes) {
        delegate.setEnabledEventTypes(enabledEventTypes);
    }


    public Boolean isAdminEventsEnabled() {
        return delegate.isAdminEventsEnabled();
    }


    public void setAdminEventsEnabled(Boolean adminEventsEnabled) {
        delegate.setAdminEventsEnabled(adminEventsEnabled);
    }


    public Boolean isAdminEventsDetailsEnabled() {
        return delegate.isAdminEventsDetailsEnabled();
    }


    public void setAdminEventsDetailsEnabled(Boolean adminEventsDetailsEnabled) {
        delegate.setAdminEventsDetailsEnabled(adminEventsDetailsEnabled);
    }


//    public List<UserFederationProviderRepresentation> getUserFederationProviders() {
//        return delegate.getUserFederationProviders();
//    }
//
//
//    public void setUserFederationProviders(List<UserFederationProviderRepresentation> userFederationProviders) {
//        delegate.setUserFederationProviders(userFederationProviders);
//    }
//
//
//    public List<UserFederationMapperRepresentation> getUserFederationMappers() {
//        return delegate.getUserFederationMappers();
//    }
//
//
//    public void setUserFederationMappers(List<UserFederationMapperRepresentation> userFederationMappers) {
//        delegate.setUserFederationMappers(userFederationMappers);
//    }
//
//
//    public void addUserFederationMapper(UserFederationMapperRepresentation userFederationMapper) {
//        delegate.addUserFederationMapper(userFederationMapper);
//    }
//
//
//    public List<IdentityProviderRepresentation> getIdentityProviders() {
//        return delegate.getIdentityProviders();
//    }
//
//
//    public void setIdentityProviders(List<IdentityProviderRepresentation> identityProviders) {
//        delegate.setIdentityProviders(identityProviders);
//    }
//
//
//    public void addIdentityProvider(IdentityProviderRepresentation identityProviderRepresentation) {
//        delegate.addIdentityProvider(identityProviderRepresentation);
//    }
//
//
//    public List<ProtocolMapperRepresentation> getProtocolMappers() {
//        return delegate.getProtocolMappers();
//    }
//
//
//    public void addProtocolMapper(ProtocolMapperRepresentation rep) {
//        delegate.addProtocolMapper(rep);
//    }
//
//
//    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
//        delegate.setProtocolMappers(protocolMappers);
//    }


    public Boolean isInternationalizationEnabled() {
        return delegate.isInternationalizationEnabled();
    }


    public void setInternationalizationEnabled(Boolean internationalizationEnabled) {
        delegate.setInternationalizationEnabled(internationalizationEnabled);
    }


    public Set<String> getSupportedLocales() {
        return delegate.getSupportedLocales();
    }


    public void addSupportedLocales(String locale) {
        delegate.addSupportedLocales(locale);
    }


    public void setSupportedLocales(Set<String> supportedLocales) {
        delegate.setSupportedLocales(supportedLocales);
    }


    public String getDefaultLocale() {
        return delegate.getDefaultLocale();
    }


    public void setDefaultLocale(String defaultLocale) {
        delegate.setDefaultLocale(defaultLocale);
    }


//    public List<IdentityProviderMapperRepresentation> getIdentityProviderMappers() {
//        return delegate.getIdentityProviderMappers();
//    }
//
//
//    public void setIdentityProviderMappers(List<IdentityProviderMapperRepresentation> identityProviderMappers) {
//        delegate.setIdentityProviderMappers(identityProviderMappers);
//    }
//
//
//    public void addIdentityProviderMapper(IdentityProviderMapperRepresentation rep) {
//        delegate.addIdentityProviderMapper(rep);
//    }


//    public List<AuthenticationFlowRepresentation> getAuthenticationFlows() {
//        return delegate.getAuthenticationFlows();
//    }
//
//
//    public void setAuthenticationFlows(List<AuthenticationFlowRepresentation> authenticationFlows) {
//        delegate.setAuthenticationFlows(authenticationFlows);
//    }
//
//
//    public List<AuthenticatorConfigRepresentation> getAuthenticatorConfig() {
//        return delegate.getAuthenticatorConfig();
//    }
//
//
//    public void setAuthenticatorConfig(List<AuthenticatorConfigRepresentation> authenticatorConfig) {
//        delegate.setAuthenticatorConfig(authenticatorConfig);
//    }
//
//
//    public List<RequiredActionProviderRepresentation> getRequiredActions() {
//        return delegate.getRequiredActions();
//    }
//
//
//    public void setRequiredActions(List<RequiredActionProviderRepresentation> requiredActions) {
//        delegate.setRequiredActions(requiredActions);
//    }


    public String getOtpPolicyType() {
        return delegate.getOtpPolicyType();
    }


    public void setOtpPolicyType(String otpPolicyType) {
        delegate.setOtpPolicyType(otpPolicyType);
    }


    public String getOtpPolicyAlgorithm() {
        return delegate.getOtpPolicyAlgorithm();
    }


    public void setOtpPolicyAlgorithm(String otpPolicyAlgorithm) {
        delegate.setOtpPolicyAlgorithm(otpPolicyAlgorithm);
    }


    public Integer getOtpPolicyInitialCounter() {
        return delegate.getOtpPolicyInitialCounter();
    }


    public void setOtpPolicyInitialCounter(Integer otpPolicyInitialCounter) {
        delegate.setOtpPolicyInitialCounter(otpPolicyInitialCounter);
    }


    public Integer getOtpPolicyDigits() {
        return delegate.getOtpPolicyDigits();
    }


    public void setOtpPolicyDigits(Integer otpPolicyDigits) {
        delegate.setOtpPolicyDigits(otpPolicyDigits);
    }


    public Integer getOtpPolicyLookAheadWindow() {
        return delegate.getOtpPolicyLookAheadWindow();
    }


    public void setOtpPolicyLookAheadWindow(Integer otpPolicyLookAheadWindow) {
        delegate.setOtpPolicyLookAheadWindow(otpPolicyLookAheadWindow);
    }


    public Integer getOtpPolicyPeriod() {
        return delegate.getOtpPolicyPeriod();
    }


    public void setOtpPolicyPeriod(Integer otpPolicyPeriod) {
        delegate.setOtpPolicyPeriod(otpPolicyPeriod);
    }


    public List<String> getOtpSupportedApplications() {
        return delegate.getOtpSupportedApplications();
    }


    public void setOtpSupportedApplications(List<String> otpSupportedApplications) {
        delegate.setOtpSupportedApplications(otpSupportedApplications);
    }


    public Map<String, Map<String, String>> getLocalizationTexts() {
        return delegate.getLocalizationTexts();
    }


    public void setLocalizationTexts(Map<String, Map<String, String>> localizationTexts) {
        delegate.setLocalizationTexts(localizationTexts);
    }


    public Boolean isOtpPolicyCodeReusable() {
        return delegate.isOtpPolicyCodeReusable();
    }


    public void setOtpPolicyCodeReusable(Boolean isCodeReusable) {
        delegate.setOtpPolicyCodeReusable(isCodeReusable);
    }


    public String getWebAuthnPolicyRpEntityName() {
        return delegate.getWebAuthnPolicyRpEntityName();
    }


    public void setWebAuthnPolicyRpEntityName(String webAuthnPolicyRpEntityName) {
        delegate.setWebAuthnPolicyRpEntityName(webAuthnPolicyRpEntityName);
    }


    public List<String> getWebAuthnPolicySignatureAlgorithms() {
        return delegate.getWebAuthnPolicySignatureAlgorithms();
    }


    public void setWebAuthnPolicySignatureAlgorithms(List<String> webAuthnPolicySignatureAlgorithms) {
        delegate.setWebAuthnPolicySignatureAlgorithms(webAuthnPolicySignatureAlgorithms);
    }


    public String getWebAuthnPolicyRpId() {
        return delegate.getWebAuthnPolicyRpId();
    }


    public void setWebAuthnPolicyRpId(String webAuthnPolicyRpId) {
        delegate.setWebAuthnPolicyRpId(webAuthnPolicyRpId);
    }


    public String getWebAuthnPolicyAttestationConveyancePreference() {
        return delegate.getWebAuthnPolicyAttestationConveyancePreference();
    }


    public void setWebAuthnPolicyAttestationConveyancePreference(String webAuthnPolicyAttestationConveyancePreference) {
        delegate.setWebAuthnPolicyAttestationConveyancePreference(webAuthnPolicyAttestationConveyancePreference);
    }


    public String getWebAuthnPolicyAuthenticatorAttachment() {
        return delegate.getWebAuthnPolicyAuthenticatorAttachment();
    }


    public void setWebAuthnPolicyAuthenticatorAttachment(String webAuthnPolicyAuthenticatorAttachment) {
        delegate.setWebAuthnPolicyAuthenticatorAttachment(webAuthnPolicyAuthenticatorAttachment);
    }


    public String getWebAuthnPolicyRequireResidentKey() {
        return delegate.getWebAuthnPolicyRequireResidentKey();
    }


    public void setWebAuthnPolicyRequireResidentKey(String webAuthnPolicyRequireResidentKey) {
        delegate.setWebAuthnPolicyRequireResidentKey(webAuthnPolicyRequireResidentKey);
    }


    public String getWebAuthnPolicyUserVerificationRequirement() {
        return delegate.getWebAuthnPolicyUserVerificationRequirement();
    }


    public void setWebAuthnPolicyUserVerificationRequirement(String webAuthnPolicyUserVerificationRequirement) {
        delegate.setWebAuthnPolicyUserVerificationRequirement(webAuthnPolicyUserVerificationRequirement);
    }


    public Integer getWebAuthnPolicyCreateTimeout() {
        return delegate.getWebAuthnPolicyCreateTimeout();
    }


    public void setWebAuthnPolicyCreateTimeout(Integer webAuthnPolicyCreateTimeout) {
        delegate.setWebAuthnPolicyCreateTimeout(webAuthnPolicyCreateTimeout);
    }


    public Boolean isWebAuthnPolicyAvoidSameAuthenticatorRegister() {
        return delegate.isWebAuthnPolicyAvoidSameAuthenticatorRegister();
    }


    public void setWebAuthnPolicyAvoidSameAuthenticatorRegister(Boolean webAuthnPolicyAvoidSameAuthenticatorRegister) {
        delegate.setWebAuthnPolicyAvoidSameAuthenticatorRegister(webAuthnPolicyAvoidSameAuthenticatorRegister);
    }


    public List<String> getWebAuthnPolicyAcceptableAaguids() {
        return delegate.getWebAuthnPolicyAcceptableAaguids();
    }


    public void setWebAuthnPolicyAcceptableAaguids(List<String> webAuthnPolicyAcceptableAaguids) {
        delegate.setWebAuthnPolicyAcceptableAaguids(webAuthnPolicyAcceptableAaguids);
    }


    public List<String> getWebAuthnPolicyExtraOrigins() {
        return delegate.getWebAuthnPolicyExtraOrigins();
    }


    public void setWebAuthnPolicyExtraOrigins(List<String> extraOrigins) {
        delegate.setWebAuthnPolicyExtraOrigins(extraOrigins);
    }


    public String getWebAuthnPolicyPasswordlessRpEntityName() {
        return delegate.getWebAuthnPolicyPasswordlessRpEntityName();
    }


    public void setWebAuthnPolicyPasswordlessRpEntityName(String webAuthnPolicyPasswordlessRpEntityName) {
        delegate.setWebAuthnPolicyPasswordlessRpEntityName(webAuthnPolicyPasswordlessRpEntityName);
    }


    public List<String> getWebAuthnPolicyPasswordlessSignatureAlgorithms() {
        return delegate.getWebAuthnPolicyPasswordlessSignatureAlgorithms();
    }


    public void setWebAuthnPolicyPasswordlessSignatureAlgorithms(List<String> webAuthnPolicyPasswordlessSignatureAlgorithms) {
        delegate.setWebAuthnPolicyPasswordlessSignatureAlgorithms(webAuthnPolicyPasswordlessSignatureAlgorithms);
    }


    public String getWebAuthnPolicyPasswordlessRpId() {
        return delegate.getWebAuthnPolicyPasswordlessRpId();
    }


    public void setWebAuthnPolicyPasswordlessRpId(String webAuthnPolicyPasswordlessRpId) {
        delegate.setWebAuthnPolicyPasswordlessRpId(webAuthnPolicyPasswordlessRpId);
    }


    public String getWebAuthnPolicyPasswordlessAttestationConveyancePreference() {
        return delegate.getWebAuthnPolicyPasswordlessAttestationConveyancePreference();
    }


    public void setWebAuthnPolicyPasswordlessAttestationConveyancePreference(String webAuthnPolicyPasswordlessAttestationConveyancePreference) {
        delegate.setWebAuthnPolicyPasswordlessAttestationConveyancePreference(webAuthnPolicyPasswordlessAttestationConveyancePreference);
    }


    public String getWebAuthnPolicyPasswordlessAuthenticatorAttachment() {
        return delegate.getWebAuthnPolicyPasswordlessAuthenticatorAttachment();
    }


    public void setWebAuthnPolicyPasswordlessAuthenticatorAttachment(String webAuthnPolicyPasswordlessAuthenticatorAttachment) {
        delegate.setWebAuthnPolicyPasswordlessAuthenticatorAttachment(webAuthnPolicyPasswordlessAuthenticatorAttachment);
    }


    public String getWebAuthnPolicyPasswordlessRequireResidentKey() {
        return delegate.getWebAuthnPolicyPasswordlessRequireResidentKey();
    }


    public void setWebAuthnPolicyPasswordlessRequireResidentKey(String webAuthnPolicyPasswordlessRequireResidentKey) {
        delegate.setWebAuthnPolicyPasswordlessRequireResidentKey(webAuthnPolicyPasswordlessRequireResidentKey);
    }


    public String getWebAuthnPolicyPasswordlessUserVerificationRequirement() {
        return delegate.getWebAuthnPolicyPasswordlessUserVerificationRequirement();
    }


    public void setWebAuthnPolicyPasswordlessUserVerificationRequirement(String webAuthnPolicyPasswordlessUserVerificationRequirement) {
        delegate.setWebAuthnPolicyPasswordlessUserVerificationRequirement(webAuthnPolicyPasswordlessUserVerificationRequirement);
    }


    public Integer getWebAuthnPolicyPasswordlessCreateTimeout() {
        return delegate.getWebAuthnPolicyPasswordlessCreateTimeout();
    }


    public void setWebAuthnPolicyPasswordlessCreateTimeout(Integer webAuthnPolicyPasswordlessCreateTimeout) {
        delegate.setWebAuthnPolicyPasswordlessCreateTimeout(webAuthnPolicyPasswordlessCreateTimeout);
    }


    public Boolean isWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister() {
        return delegate.isWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister();
    }


    public void setWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister(Boolean webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister) {
        delegate.setWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister(webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister);
    }


    public List<String> getWebAuthnPolicyPasswordlessAcceptableAaguids() {
        return delegate.getWebAuthnPolicyPasswordlessAcceptableAaguids();
    }


    public void setWebAuthnPolicyPasswordlessAcceptableAaguids(List<String> webAuthnPolicyPasswordlessAcceptableAaguids) {
        delegate.setWebAuthnPolicyPasswordlessAcceptableAaguids(webAuthnPolicyPasswordlessAcceptableAaguids);
    }


    public List<String> getWebAuthnPolicyPasswordlessExtraOrigins() {
        return delegate.getWebAuthnPolicyPasswordlessExtraOrigins();
    }


    public void setWebAuthnPolicyPasswordlessExtraOrigins(List<String> extraOrigins) {
        delegate.setWebAuthnPolicyPasswordlessExtraOrigins(extraOrigins);
    }


    @GraphQLQuery
    public Page<ClientProfileType> getClientProfiles(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100") int limit) {
        Page<ClientProfileType> ret = Page.emptyPage();

        ClientProfilesRepresentation rep = delegate.getParsedClientProfiles();

        if (rep != null) {
            List<ClientProfileRepresentation> profiles = rep.getProfiles();
            List<ClientProfileType> cpt = profiles.stream().skip(start).limit(limit).map(ClientProfileType::new).toList();
            ret = new Page<>(profiles.size(), limit, cpt);
        }

        return ret;
    }

    @GraphQLQuery
    public Page<ClientProfileType> getGlobalClientProfiles(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100") int limit) {
        Page<ClientProfileType> ret = Page.emptyPage();

        ClientProfilesRepresentation rep = delegate.getParsedClientProfiles();

        if (rep != null) {
            List<ClientProfileRepresentation> profiles = rep.getGlobalProfiles();
            List<ClientProfileType> cpt = profiles.stream().skip(start).limit(limit).map(ClientProfileType::new).toList();
            ret = new Page<>(profiles.size(), limit, cpt);
        }

        return ret;
    }

    public ClientPoliciesRepresentation getParsedClientPolicies() {
        return delegate.getParsedClientPolicies();
    }
//
//
//    public void setParsedClientPolicies(ClientPoliciesRepresentation clientPolicies) {
//        delegate.setParsedClientPolicies(clientPolicies);
//    }


    public String getBrowserFlow() {
        return delegate.getBrowserFlow();
    }


    public void setBrowserFlow(String browserFlow) {
        delegate.setBrowserFlow(browserFlow);
    }


    public String getRegistrationFlow() {
        return delegate.getRegistrationFlow();
    }


    public void setRegistrationFlow(String registrationFlow) {
        delegate.setRegistrationFlow(registrationFlow);
    }


    public String getDirectGrantFlow() {
        return delegate.getDirectGrantFlow();
    }


    public void setDirectGrantFlow(String directGrantFlow) {
        delegate.setDirectGrantFlow(directGrantFlow);
    }


    public String getResetCredentialsFlow() {
        return delegate.getResetCredentialsFlow();
    }


    public void setResetCredentialsFlow(String resetCredentialsFlow) {
        delegate.setResetCredentialsFlow(resetCredentialsFlow);
    }


    public String getClientAuthenticationFlow() {
        return delegate.getClientAuthenticationFlow();
    }


    public void setClientAuthenticationFlow(String clientAuthenticationFlow) {
        delegate.setClientAuthenticationFlow(clientAuthenticationFlow);
    }


    public String getDockerAuthenticationFlow() {
        return delegate.getDockerAuthenticationFlow();
    }


    public RealmRepresentation setDockerAuthenticationFlow(String dockerAuthenticationFlow) {
        return delegate.setDockerAuthenticationFlow(dockerAuthenticationFlow);
    }


    public String getFirstBrokerLoginFlow() {
        return delegate.getFirstBrokerLoginFlow();
    }


    public RealmRepresentation setFirstBrokerLoginFlow(String firstBrokerLoginFlow) {
        return delegate.setFirstBrokerLoginFlow(firstBrokerLoginFlow);
    }


    public String getKeycloakVersion() {
        return delegate.getKeycloakVersion();
    }


    public void setKeycloakVersion(String keycloakVersion) {
        delegate.setKeycloakVersion(keycloakVersion);
    }

    public Page<ClientScopeType> getClientScopes(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<ClientScopeType> clientScopes = delegate.getClientScopes().stream().skip(start).limit(limit).map(ClientScopeType::new).toList();
        return new Page<>(clientScopes.size(), limit, clientScopes);
    }

//    public void setClientScopes(List<ClientScopeRepresentation> clientScopes) {
//        delegate.setClientScopes(clientScopes);
//    }


//    public List<String> getDefaultDefaultClientScopes() {
//        return delegate.getDefaultDefaultClientScopes();
//    }
//
//
//    public void setDefaultDefaultClientScopes(List<String> defaultDefaultClientScopes) {
//        delegate.setDefaultDefaultClientScopes(defaultDefaultClientScopes);
//    }
//
//
//    public List<String> getDefaultOptionalClientScopes() {
//        return delegate.getDefaultOptionalClientScopes();
//    }
//
//
//    public void setDefaultOptionalClientScopes(List<String> defaultOptionalClientScopes) {
//        delegate.setDefaultOptionalClientScopes(defaultOptionalClientScopes);
//    }


//    public MultivaluedHashMap<String, ComponentExportRepresentation> getComponents() {
//        return delegate.getComponents();
//    }

    @GraphQLQuery
    public ComponentMap getComponents(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        RealmModel realm = getRealmModel();
        MultivaluedHashMap<String, ComponentExportRepresentation> components = ExportUtils.exportComponents(realm, realm.getId());

        Map<String, List<ComponentType>> comps = components.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(ComponentType::new).toList()));

        return new ComponentMap(comps, start, limit);
    }

//
//    public void setComponents(MultivaluedHashMap<String, ComponentExportRepresentation> components) {
//        delegate.setComponents(components);
//    }


    public boolean isIdentityFederationEnabled() {
        return delegate.isIdentityFederationEnabled();
    }


    public void setAttributes(Map<String, String> attributes) {
        delegate.setAttributes(attributes);
    }


    @GraphQLQuery
    public AttributeMap getAttributes(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        return new AttributeMap(delegate.getAttributes(), start, limit);
    }



    @GraphQLQuery
    public Page<UserType> getFederatedUsers(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<UserRepresentation> federatedUsers = delegate.getFederatedUsers();
        List<UserType> users = federatedUsers.stream()
                .skip(start)
                .limit(limit)
                .map(f -> new UserType(getKeycloakSession(), getRealmModel(), f))
                .toList();

        return new Page<>(federatedUsers.size(), limit, users);
    }
//
//
//    public void setFederatedUsers(List<UserRepresentation> federatedUsers) {
//        delegate.setFederatedUsers(federatedUsers);
//    }


    public void setUserManagedAccessAllowed(Boolean userManagedAccessAllowed) {
        delegate.setUserManagedAccessAllowed(userManagedAccessAllowed);
    }


    public Boolean isUserManagedAccessAllowed() {
        return delegate.isUserManagedAccessAllowed();
    }


    public Boolean isOrganizationsEnabled() {
        return delegate.isOrganizationsEnabled();
    }


    public void setOrganizationsEnabled(Boolean organizationsEnabled) {
        delegate.setOrganizationsEnabled(organizationsEnabled);
    }


    public Map<String, String> getAttributesOrEmpty() {
        return delegate.getAttributesOrEmpty();
    }


    public Page<OrganizationType> getOrganizations(@GraphQLArgument(defaultValue = "0")int start, @GraphQLArgument(defaultValue = "100")int limit) {
        List<OrganizationRepresentation> organizations = delegate.getOrganizations();
        List<OrganizationType> organizationTypes = organizations.stream().skip(start).limit(100).map(o -> new OrganizationType(getKeycloakSession(), o)).toList();

        return new Page<>(organizations.size(), limit, organizationTypes);
    }
//
//
//    public void setOrganizations(List<OrganizationRepresentation> organizations) {
//        delegate.setOrganizations(organizations);
//    }
//
//
//    public void addOrganization(OrganizationRepresentation org) {
//        delegate.addOrganization(org);
//    }
}
