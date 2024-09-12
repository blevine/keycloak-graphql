package net.brianlevine.keycloak.graphql.util;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;

public class Util {
    public static AdminEventBuilder createAdminEventBuilder(KeycloakSession session, RealmModel realm, AdminAuth auth) {
        return new AdminEventBuilder(realm, auth, session, session.getContext().getConnection());
    }
}