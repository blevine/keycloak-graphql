package net.brianlevine.keycloak.graphql.subscriptions;

import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLSubscription;
import io.leangen.graphql.execution.ResolutionEnvironment;
import net.brianlevine.keycloak.graphql.events.MulticastEventListenerProviderFactory;
import net.brianlevine.keycloak.graphql.types.KeycloakEventType;
import org.reactivestreams.Publisher;


public class EventsSubscription {


    @GraphQLSubscription(description = "Subscribe to Keycloak events.")
    public Publisher<KeycloakEventType> events(@GraphQLEnvironment ResolutionEnvironment env) {
        // Note: can add .filter() here if we have an argument that specified what event
        //       types to return.
        return MulticastEventListenerProviderFactory.getEventMulticastProcessor(env);
    }
}
