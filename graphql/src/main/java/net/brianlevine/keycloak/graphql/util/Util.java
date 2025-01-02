package net.brianlevine.keycloak.graphql.util;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static AdminEventBuilder createAdminEventBuilder(KeycloakSession session, RealmModel realm, AdminAuth auth) {
        return new AdminEventBuilder(realm, auth, session, session.getContext().getConnection());
    }

    public static String toIsoDateTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        return dateFormat.format(date);
    }

    public static HttpHeaders fakeHttpHeadersWithToken(String accessToken) {
        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return new HttpHeaderWrapper(headerMap);
    }
}