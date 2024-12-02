package net.brianlevine.keycloak.graphql;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;


/**
 * WebSocket server using Tyrus that supports the Apollo graphql-transport-ws sub-protocol
 */
@ServerEndpoint(value = "/graphql", subprotocols = {"graphql-transport-ws"})
public class ApolloTyrusServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloTyrusServer.class);
    private final WebSocketProtocolHandler protocolHandler;

    public ApolloTyrusServer() {
        protocolHandler = new ApolloProtocolHandler();
    }


    @OnOpen
    public void onOpen(Session session) {
        protocolHandler.onOpen(session);
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        return protocolHandler.onMessage(message, session);
    }

    @OnMessage
    public String onMessage(byte[] message, Session session) {
        return protocolHandler.onMessage(message, session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        protocolHandler.onError(session, error);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        protocolHandler.onClose(session, closeReason);
    }



    public static void runServer() {
        Server server = new Server("localhost", 8081, "/", null, ApolloTyrusServer.class);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {

                LOGGER.info("Starting ApolloTyrusServer");
                server.start();

                LOGGER.info("ApolloTyrusServer running on port {}.", server.getPort());
                Thread.currentThread().join();
            } catch (Throwable e) {
                LOGGER.error("ApolloTyrusServer failed to start on port {}.", server.getPort(), e);
            } finally {
                LOGGER.info("ApolloTyrusServer stopping.");
                server.stop();
            }
        });

    }
}
