package net.brianlevine.keycloak.graphql.subscriptions;

import io.leangen.graphql.annotations.GraphQLSubscription;
import net.brianlevine.keycloak.graphql.events.MulticastEventListenerProviderFactory;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.reactivestreams.Publisher;


public class EventsSubscription {


    @GraphQLSubscription(description = "Subscribe to Keycloak events.")
    public Publisher<Event> events(/*@GraphQLEnvironment ResolutionEnvironment env*/) {
        // Note: can add .filter() here if we have an argument that specified what event
        //       types to return.
        return MulticastEventListenerProviderFactory.getEventMulticastProcessor();
    }

    @GraphQLSubscription(description = "Subscribe to Keycloak Admin Events.")
    public Publisher<AdminEvent> adminEvents(/*@GraphQLEnvironment ResolutionEnvironment env*/) {
        // Note: can add .filter() here if we have an argument that specified what event
        //       types to return.
        return MulticastEventListenerProviderFactory.getAdminEventMulticastProcessor();
    }
}
