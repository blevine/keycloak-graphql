package net.brianlevine.keycloak.graphql;

import jakarta.websocket.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Session store backed by a ConcurrentHashMap
 */
public class DefaultSessionStore implements SessionStore {
    Map<String, Session> map = new ConcurrentHashMap<>();

    public DefaultSessionStore() {

    }
    @Override
    public Map<String, Session> getAllSessions() {
        return map;
    }

    @Override
    public Session getSession(String sessionId) {
        return map.get(sessionId);
    }

    @Override
    public void addSession(Session session) {
        map.put(session.getId(), session);
    }

    @Override
    public void removeSession(String sessionId) {
        map.remove(sessionId);
    }
}
