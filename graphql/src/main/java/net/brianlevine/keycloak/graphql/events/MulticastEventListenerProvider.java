package net.brianlevine.keycloak.graphql.events;

import io.reactivex.rxjava3.processors.MulticastProcessor;
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

    private final MulticastProcessor<Event> eventMulticastProcessor;
    private final MulticastProcessor<AdminEvent> adminEventMulticastProcessor;

    public MulticastEventListenerProvider(MulticastProcessor<Event> eventMulticastProcessor, MulticastProcessor<AdminEvent> adminEventMulticastProcessor) {
        this.eventMulticastProcessor = eventMulticastProcessor;
        this.adminEventMulticastProcessor = adminEventMulticastProcessor;
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
            boolean res = eventMulticastProcessor.offer(e);

            if (!res) {
                LOGGER.warn("multicastProcessor.offer() returned false");
            }
        }
    }

    private void sendEvent(AdminEvent e) {
        if (adminEventMulticastProcessor.hasSubscribers()) {
            boolean res = adminEventMulticastProcessor.offer(e);

            if (!res) {
                LOGGER.warn("multicastProcessor.offer() returned false");
            }
        }
    }


    @Override
    public void close() {
    }
}
