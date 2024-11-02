package net.brianlevine.keycloak.graphql.events;

import com.google.auto.service.AutoService;
import io.reactivex.rxjava3.processors.MulticastProcessor;

import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Creates MulticastEventListenerProviders for Keycloak Events and AdminEvents
 */
@AutoService(EventListenerProviderFactory.class)
public class MulticastEventListenerProviderFactory implements EventListenerProviderFactory {
    private static MulticastProcessor<Event> eventMulticastProcessor;
    private static MulticastProcessor<AdminEvent> adminEventMulticastProcessor;

    public static MulticastProcessor<Event> getEventMulticastProcessor() {
        return eventMulticastProcessor;
    }

    public static MulticastProcessor<AdminEvent> getAdminEventMulticastProcessor() {
        return adminEventMulticastProcessor;
    }

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new MulticastEventListenerProvider(eventMulticastProcessor, adminEventMulticastProcessor);
    }

    @Override
    public void init(Config.Scope scope) {
        eventMulticastProcessor = MulticastProcessor.create(false);
        eventMulticastProcessor.startUnbounded();
        adminEventMulticastProcessor = MulticastProcessor.create(false);
        adminEventMulticastProcessor.startUnbounded();
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "multicast-event-listener";
    }
}