package net.brianlevine.keycloak.graphql.events;

import com.google.auto.service.AutoService;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.reactivex.rxjava3.processors.MulticastProcessor;

import net.brianlevine.keycloak.graphql.types.KeycloakEventType;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates MulticastEventListenerProviders for Keycloak Events and AdminEvents
 */
@AutoService(EventListenerProviderFactory.class)
public class MulticastEventListenerProviderFactory implements EventListenerProviderFactory {
    private static MulticastProcessor<KeycloakEventType> eventMulticastProcessor;
    private static Map<MulticastProcessor<KeycloakEventType>, ResolutionEnvironment> stateTable = new HashMap<>();

    public static MulticastProcessor<KeycloakEventType> getEventMulticastProcessor(ResolutionEnvironment env) {
        stateTable.put(eventMulticastProcessor, env);
        return eventMulticastProcessor;
    }

    public static ResolutionEnvironment getState(MulticastProcessor<KeycloakEventType> m) {
        return stateTable.get(m);
    }


    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new MulticastEventListenerProvider(eventMulticastProcessor, this);
    }

    @Override
    public void init(Config.Scope scope) {
        eventMulticastProcessor = MulticastProcessor.create(false);
        eventMulticastProcessor.startUnbounded();
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