package net.brianlevine.keycloak.graphql.types;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import io.leangen.graphql.annotations.types.GraphQLType;
import io.leangen.graphql.execution.ResolutionEnvironment;
import net.brianlevine.keycloak.graphql.queries.RealmQuery;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.util.Date;
import java.util.Map;

import static net.brianlevine.keycloak.graphql.Constants.KEYCLOAK_SESSION_KEY;


@GraphQLType
public class KeycloakEventType {
    private Event event;
    private AdminEvent adminEvent;


    public KeycloakEventType(Event keycloakEvent) {
        this.event = keycloakEvent;
    }
    public KeycloakEventType(AdminEvent adminEvent) {
        this.adminEvent = adminEvent;
    }

    public String getId() {
        return event != null ? event.getId() : adminEvent.getId();
    }

    @GraphQLQuery
    public UserType getUser(@GraphQLEnvironment ResolutionEnvironment env) {
        GraphQLContext ctx = env.dataFetchingEnvironment.getGraphQlContext();
        String userId =  event != null ? event.getUserId() : adminEvent.getAuthDetails().getUserId();
        return getRealm(env).getUserById(userId, ctx);
    }


    @GraphQLQuery
    public RealmType getRealm(@GraphQLEnvironment ResolutionEnvironment env) {
        GraphQLContext ctx = env.dataFetchingEnvironment.getGraphQlContext();
        String realmId = event != null ? event.getRealmId() : adminEvent.getRealmId();
        return new RealmQuery().getRealmById(realmId, ctx);

    }


    public String getIpAddress() {
        return event != null ? event.getIpAddress() : adminEvent.getAuthDetails().getIpAddress();
    }

    @GraphQLQuery
    public ClientType getClient(@GraphQLEnvironment ResolutionEnvironment env) {
        GraphQLContext ctx = env.dataFetchingEnvironment.getGraphQlContext();
        String clientId = event != null ?  event.getClientId() : adminEvent.getAuthDetails().getClientId();
        KeycloakSession kcSession = ctx.get(KEYCLOAK_SESSION_KEY);
        RealmModel realmModel = kcSession.getContext().getRealm();
        ClientModel clientModel = kcSession.getContext().getRealm().getClientByClientId(clientId);

        return new ClientType(kcSession, realmModel, clientModel);
    }


    @GraphQLQuery(description = "Returns the event type for a standard Event or ADMIN_EVENT for an AdminEvent")
    public String getType() {
        return event != null ? event.getType().name() : "ADMIN_EVENT";
    }


    @GraphQLQuery(description = "Returns null for an AdminEvent")
    public Map<String, String> getDetails() {
        return event != null ? event.getDetails() : null;
    }

    @GraphQLQuery(description = "Returns null for an AdminEvent")
    public String getSessionId() {
        return event != null ? event.getSessionId() : null;
    }

    @GraphQLQuery
    public Date getTime() {
        return new Date(event != null ? event.getTime() : adminEvent.getTime());
    }

    public String getError() {
        return event.getError();
    }

    // AdminEvent only

    @GraphQLQuery(description = "AdminEvent-only. Returns null for standard Event.")
    public String getResourceType() {
        return adminEvent != null ? adminEvent.getResourceTypeAsString() : null;
    }

    @GraphQLQuery(description = "AdminEvent-only. Returns null for standard Event.")
    public String getOperationType() {
        return adminEvent != null ? adminEvent.getOperationType().name() : null;
    }

    @GraphQLQuery(description = "AdminEvent-only. Returns null for standard Event.")
    public String getResourcePath() {
        return adminEvent != null ? adminEvent.getResourcePath() : null;
    }

    @GraphQLQuery(description = "AdminEvent-only. Returns null for standard Event.")
    public String getRepresentation() {
        return adminEvent != null ? adminEvent.getRepresentation() : null;
    }

}

