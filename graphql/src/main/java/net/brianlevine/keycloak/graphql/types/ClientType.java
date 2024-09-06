package net.brianlevine.keycloak.graphql.types;

import graphql.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.types.GraphQLType;

import net.brianlevine.keycloak.graphql.util.Auth;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.resources.admin.permissions.RolePermissionEvaluator;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@GraphQLType
@SuppressWarnings("unused")
public class ClientType implements Container, RoleHolder, BaseType {
    private final ClientRepresentation delegate;

    @SuppressWarnings("FieldCanBeLocal")
    private final KeycloakSession kcSession;

    @SuppressWarnings("FieldCanBeLocal")
    private final RealmModel realmModel;

    private  ClientModel clientModel;

    public ClientType(KeycloakSession kcSession, RealmModel realmModel, ClientRepresentation delegate) {
        this.delegate = delegate;
        this.kcSession = kcSession;
        this.realmModel = realmModel;
    }

    public ClientType(KeycloakSession kcSession, RealmModel realmModel, ClientModel clientModel) {
        this.delegate = ModelToRepresentation.toRepresentation(clientModel, kcSession);
        this.kcSession = kcSession;
        this.realmModel = realmModel;
        this.clientModel = clientModel;
    }

    @GraphQLIgnore
    public ClientModel getClientModel() {
        if (clientModel == null) {
            clientModel = getRealmModel().getClientById(getId());
        }

        return clientModel;
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

    public String getType() {
        return delegate.getType();
    }

    public void setType(String type) {
        delegate.setType(type);
    }

    public String getClientId() {
        return delegate.getClientId();
    }

    public void setClientId(String clientId) {
        delegate.setClientId(clientId);
    }

    public Boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void setEnabled(Boolean enabled) {
        delegate.setEnabled(enabled);
    }

    public Boolean isAlwaysDisplayInConsole() {
        return delegate.isAlwaysDisplayInConsole();
    }

    public void setAlwaysDisplayInConsole(Boolean alwaysDisplayInConsole) {
        delegate.setAlwaysDisplayInConsole(alwaysDisplayInConsole);
    }

    public Boolean isSurrogateAuthRequired() {
        return delegate.isSurrogateAuthRequired();
    }

    public void setSurrogateAuthRequired(Boolean surrogateAuthRequired) {
        delegate.setSurrogateAuthRequired(surrogateAuthRequired);
    }

    public String getRootUrl() {
        return delegate.getRootUrl();
    }

    public void setRootUrl(String rootUrl) {
        delegate.setRootUrl(rootUrl);
    }

    public String getAdminUrl() {
        return delegate.getAdminUrl();
    }

    public void setAdminUrl(String adminUrl) {
        delegate.setAdminUrl(adminUrl);
    }

    public String getBaseUrl() {
        return delegate.getBaseUrl();
    }

    public void setBaseUrl(String baseUrl) {
        delegate.setBaseUrl(baseUrl);
    }

    public String getClientAuthenticatorType() {
        return delegate.getClientAuthenticatorType();
    }

    public void setClientAuthenticatorType(String clientAuthenticatorType) {
        delegate.setClientAuthenticatorType(clientAuthenticatorType);
    }

    public String getSecret() {
        return delegate.getSecret();
    }

    public void setSecret(String secret) {
        delegate.setSecret(secret);
    }

    public String getRegistrationAccessToken() {
        return delegate.getRegistrationAccessToken();
    }

    public void setRegistrationAccessToken(String registrationAccessToken) {
        delegate.setRegistrationAccessToken(registrationAccessToken);
    }

    public List<String> getRedirectUris() {
        return delegate.getRedirectUris();
    }

    public void setRedirectUris(List<String> redirectUris) {
        delegate.setRedirectUris(redirectUris);
    }

    public List<String> getWebOrigins() {
        return delegate.getWebOrigins();
    }

    public void setWebOrigins(List<String> webOrigins) {
        delegate.setWebOrigins(webOrigins);
    }

    @Deprecated
    public String[] getDefaultRoles() {
        return delegate.getDefaultRoles();
    }

    @Deprecated
    public void setDefaultRoles(String[] defaultRoles) {
        delegate.setDefaultRoles(defaultRoles);
    }

    public Integer getNotBefore() {
        return delegate.getNotBefore();
    }

    public void setNotBefore(Integer notBefore) {
        delegate.setNotBefore(notBefore);
    }

    public Boolean isBearerOnly() {
        return delegate.isBearerOnly();
    }

    public void setBearerOnly(Boolean bearerOnly) {
        delegate.setBearerOnly(bearerOnly);
    }

    public Boolean isConsentRequired() {
        return delegate.isConsentRequired();
    }

    public void setConsentRequired(Boolean consentRequired) {
        delegate.setConsentRequired(consentRequired);
    }

    public Boolean isStandardFlowEnabled() {
        return delegate.isStandardFlowEnabled();
    }

    public void setStandardFlowEnabled(Boolean standardFlowEnabled) {
        delegate.setStandardFlowEnabled(standardFlowEnabled);
    }

    public Boolean isImplicitFlowEnabled() {
        return delegate.isImplicitFlowEnabled();
    }

    public void setImplicitFlowEnabled(Boolean implicitFlowEnabled) {
        delegate.setImplicitFlowEnabled(implicitFlowEnabled);
    }

    public Boolean isDirectAccessGrantsEnabled() {
        return delegate.isDirectAccessGrantsEnabled();
    }

    public void setDirectAccessGrantsEnabled(Boolean directAccessGrantsEnabled) {
        delegate.setDirectAccessGrantsEnabled(directAccessGrantsEnabled);
    }

    public Boolean isServiceAccountsEnabled() {
        return delegate.isServiceAccountsEnabled();
    }

    public void setServiceAccountsEnabled(Boolean serviceAccountsEnabled) {
        delegate.setServiceAccountsEnabled(serviceAccountsEnabled);
    }

    public Boolean getAuthorizationServicesEnabled() {
        if (delegate.getAuthorizationSettings() != null) {
            return true;
        }
        return delegate.getAuthorizationServicesEnabled();
    }

    public void setAuthorizationServicesEnabled(Boolean authorizationServicesEnabled) {
        delegate.setAuthorizationServicesEnabled(authorizationServicesEnabled);
    }

    @Deprecated
    public Boolean isDirectGrantsOnly() {
        return delegate.isDirectGrantsOnly();
    }

    public void setDirectGrantsOnly(Boolean directGrantsOnly) {
        delegate.setDirectGrantsOnly(directGrantsOnly);
    }

    public Boolean isPublicClient() {
        return delegate.isPublicClient();
    }

    public void setPublicClient(Boolean publicClient) {
        delegate.setPublicClient(publicClient);
    }

    public Boolean isFullScopeAllowed() {
        return delegate.isFullScopeAllowed();
    }

    public void setFullScopeAllowed(Boolean fullScopeAllowed) {
        delegate.setFullScopeAllowed(fullScopeAllowed);
    }

    public String getProtocol() {
        return delegate.getProtocol();
    }

    public void setProtocol(String protocol) {
        delegate.setProtocol(protocol);
    }

    public Map<String, String> getAttributes() {
        return delegate.getAttributes();
    }

    public void setAttributes(Map<String, String> attributes) {
        delegate.setAttributes(attributes);
    }

    public Map<String, String> getAuthenticationFlowBindingOverrides() {
        return delegate.getAuthenticationFlowBindingOverrides();
    }

    public void setAuthenticationFlowBindingOverrides(Map<String, String> authenticationFlowBindingOverrides) {
        delegate.setAuthenticationFlowBindingOverrides(authenticationFlowBindingOverrides);
    }

    public Integer getNodeReRegistrationTimeout() {
        return delegate.getNodeReRegistrationTimeout();
    }

    public void setNodeReRegistrationTimeout(Integer nodeReRegistrationTimeout) {
        delegate.setNodeReRegistrationTimeout(nodeReRegistrationTimeout);
    }

    public Map<String, Integer> getRegisteredNodes() {
        return delegate.getRegisteredNodes();
    }

    public void setRegisteredNodes(Map<String, Integer> registeredNodes) {
        delegate.setRegisteredNodes(registeredNodes);
    }

    public Boolean isFrontchannelLogout() {
        return delegate.isFrontchannelLogout();
    }

    public void setFrontchannelLogout(Boolean frontchannelLogout) {
        delegate.setFrontchannelLogout(frontchannelLogout);
    }

//    public List<ProtocolMapperRepresentation> getProtocolMappers() {
//        return delegate.getProtocolMappers();
//    }
//
//    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
//        delegate.setProtocolMappers(protocolMappers);
//    }

    @Deprecated
    public String getClientTemplate() {
        return delegate.getClientTemplate();
    }

    @Deprecated
    public Boolean isUseTemplateConfig() {
        return delegate.isUseTemplateConfig();
    }

    @Deprecated
    public Boolean isUseTemplateScope() {
        return delegate.isUseTemplateScope();
    }

    @Deprecated
    public Boolean isUseTemplateMappers() {
        return delegate.isUseTemplateMappers();
    }

    public List<String> getDefaultClientScopes() {
        return delegate.getDefaultClientScopes();
    }

    public void setDefaultClientScopes(List<String> defaultClientScopes) {
        delegate.setDefaultClientScopes(defaultClientScopes);
    }

    public List<String> getOptionalClientScopes() {
        return delegate.getOptionalClientScopes();
    }

    public void setOptionalClientScopes(List<String> optionalClientScopes) {
        delegate.setOptionalClientScopes(optionalClientScopes);
    }

//    public ResourceServerRepresentation getAuthorizationSettings() {
//        return delegate.getAuthorizationSettings();
//    }
//
//    public void setAuthorizationSettings(ResourceServerRepresentation authorizationSettings) {
//        delegate.setAuthorizationSettings(authorizationSettings);
//    }

    public Map<String, Boolean> getAccess() {
        return delegate.getAccess();
    }

    public void setAccess(Map<String, Boolean> access) {
        delegate.setAccess(access);
    }

    /**
     * Returns id of ClientStorageProvider that loaded this user
     *
     * @return NULL if user stored locally
     */
    public String getOrigin() {
        return delegate.getOrigin();
    }

    public void setOrigin(String origin) {
        delegate.setOrigin(origin);
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
    public RealmModel getRealmModel() {
        return realmModel;
    }

    @Override
    @GraphQLIgnore
    public KeycloakSession getKeycloakSession() {
        return kcSession;
    }
}
