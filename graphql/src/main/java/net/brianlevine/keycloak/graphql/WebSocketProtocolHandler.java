package net.brianlevine.keycloak.graphql;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;


public interface WebSocketProtocolHandler {

    void onOpen(Session session);
    String onMessage(String message, Session session);

    default String onMessage(byte[] message, Session session) {
        return onMessage(new String(message), session);
    };

    void onError(Session session, Throwable error);
    void onClose(Session session, CloseReason closeReason);
}
