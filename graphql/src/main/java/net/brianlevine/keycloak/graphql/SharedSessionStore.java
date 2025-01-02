package net.brianlevine.keycloak.graphql;

import jakarta.websocket.Session;

import java.util.Map;

public class SharedSessionStore  {
    // Note: Can use a distributed store impl when this needs to run in a cluster.
    // TODO: Mechanism for registering session store impl.
    private final static SessionStore INSTANCE = new DefaultSessionStore();

    public static SessionStore getInstance() {
        return INSTANCE;
    }
}
