package net.brianlevine.keycloak.graphql;

import jakarta.websocket.Session;

import java.util.Map;

public interface SessionStore {
    Map<String, Session> getAllSessions();
    Session getSession(String sessionId);
    void addSession(Session session);
    void removeSession(String sessionId);
}
