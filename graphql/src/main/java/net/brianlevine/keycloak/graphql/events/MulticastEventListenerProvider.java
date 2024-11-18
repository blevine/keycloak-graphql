package net.brianlevine.keycloak.graphql.events;

import io.reactivex.rxjava3.processors.MulticastProcessor;
import net.brianlevine.keycloak.graphql.types.KeycloakEventType;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;



/**
 * An EventListenerProvider that sends Keycloak Events and AdminEvents to
 * a reactive MulticaseProcessor which can be subscribed to to receive those events.
 * This is used to implement the server side for Event and AdminEvent GraphQL subscriptions.
 */
public class MulticastEventListenerProvider
        implements EventListenerProvider {

    private static final Logger LOGGER = Logger.getLogger(MulticastEventListenerProvider.class);

    private final MulticastProcessor<KeycloakEventType> eventMulticastProcessor;
    private final MulticastEventListenerProviderFactory fac;


    public MulticastEventListenerProvider(MulticastProcessor<KeycloakEventType> eventMulticastProcessor, MulticastEventListenerProviderFactory fac) {
        this.eventMulticastProcessor = eventMulticastProcessor;
        this.fac = fac;
    }

    @Override
    public void onEvent(Event event) {
        sendEvent(event);
    }


    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        sendEvent(adminEvent);
    }

    private void sendEvent(Event e) {
        if (eventMulticastProcessor.hasSubscribers()) {
            boolean res = eventMulticastProcessor.offer(new KeycloakEventType(e));

            if (!res) {
                LOGGER.warn("multicastProcessor.offer() returned false");
            }
        }
    }

    private void sendEvent(AdminEvent e) {
        if (eventMulticastProcessor.hasSubscribers()) {
            boolean res = eventMulticastProcessor.offer(new KeycloakEventType(e));

            if (!res) {
                LOGGER.warn("multicastProcessor.offer() returned false");
            }
        }
    }


    @Override
    public void close() {
    }
}
