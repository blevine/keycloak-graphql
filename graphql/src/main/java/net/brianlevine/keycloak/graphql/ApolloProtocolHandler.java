package net.brianlevine.keycloak.graphql;


import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.ExecutionResult;

import graphql.GraphQLError;
import io.netty.handler.codec.http.QueryStringDecoder;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import net.brianlevine.keycloak.graphql.apollo.ApolloMessage;
import net.brianlevine.keycloak.graphql.apollo.MessageType;
import net.brianlevine.keycloak.graphql.apollo.SubscribePayload;
import net.brianlevine.keycloak.graphql.util.HttpHeaderWrapper;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ApolloProtocolHandler implements WebSocketProtocolHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloProtocolHandler.class);

    private final Map<String, Disposable> subscriptions = new ConcurrentHashMap<>();
    private final GraphQLController graphQL;

    public ApolloProtocolHandler() {
        this.graphQL = new GraphQLController();
    }

    @Override
    public void onOpen(Session session) {
        LOGGER.info("Opened: session id = {}", session.getId());
    }

    @Override
    public String onMessage(String message, Session session) {
        System.out.println(message);
        return dispatch(session, message);
    }

    @Override
    public void onError(Session session, Throwable error) {
        LOGGER.error("Error: session id = {}", session.getId(), error);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        //if (keepAlive != null) {
        //    keepAlive.cancel(true);
        //}
        LOGGER.info("Closed: session id = {}, reason = {}", session.getId(), closeReason.getReasonPhrase());
    }

    protected String dispatch(Session session, String message) {
        // TODO: Can we use session.getPathParameters()???
        URI uri = session.getRequestURI();
        QueryStringDecoder d = new QueryStringDecoder(uri);
        Map<String, List<String>> queryParams = d.parameters();

        String accessToken = null;

        if (queryParams != null) {
            List<String> p = queryParams.get("access_token");

            if (p != null) {
                accessToken = p.get(0);
            }
        }

        String response = null;
        try {
            try {
                ApolloMessage apolloMessage = ApolloMessage.fromJson(message);

                switch (apolloMessage.getType()) {
                    case ConnectionInit:
                        response = doConnectionInit(session, apolloMessage);
                        break;
                    case Ping:
                        response = doPing(session, apolloMessage);
                        break;
                    case Pong:
                        response = doPong(session, apolloMessage);
                        break;
                    case Subscribe:
                        response = doSubscribe(session, apolloMessage, accessToken);
                        break;
                    case Complete:
                        response = doComplete(session, apolloMessage);
                        break;
                    default:
                        session.close(new CloseReason(ApolloCode.BadRequest, String.format("Unknown message type %s", apolloMessage.getType())));
                        break;
                }


            } catch (IOException e) {
                session.close(new CloseReason(ApolloCode.BadRequest, String.format("Error processing message %s: %s", message, e.getMessage())));
            }
        } catch (Exception e) {
            fatalError(session, e);
        }

        return response;
    }

    private String doConnectionInit(Session session, ApolloMessage message) throws JsonProcessingException {
        ApolloMessage response = new ApolloMessage(MessageType.ConnectionAck, message.getPayload());
        return response.toJson();
    }

    private String doPing(Session session, ApolloMessage message) throws JsonProcessingException{
        ApolloMessage response = new ApolloMessage(MessageType.Pong, message.getPayload());
        return response.toJson();
    }

    private String doPong(Session session, ApolloMessage message) throws JsonProcessingException{
        LOGGER.info("Got a PONG message for session id = {}, with payload {}.", session.getId(), message.getPayload());
        return null;
    }

    private String doSubscribe(Session session, ApolloMessage message, String accessToken) throws JsonProcessingException {
        ExecutionResult er = execute(session, message, accessToken);

        // TODO: Do we need to return a response here (Next?) or do we just allow the subscription to handle
        //       that when an event occurs?
        return null;
    }

    private String doComplete(Session session, ApolloMessage message) throws JsonProcessingException{
        String id = message.getId();
        LOGGER.info("Got a COMPLETE message for id '{}'.", id);
        cancelSubscription(id);

        return null;
    }

    private ExecutionResult execute(Session session, ApolloMessage apolloMessage, String accessToken) {
        SubscribePayload payload = (SubscribePayload)apolloMessage.getPayload();

        QuarkusKeycloakSessionFactory fac = QuarkusKeycloakSessionFactory.getInstance();
        fac.init();
        KeycloakSession kcSession = fac.create();
        kcSession.getTransactionManager().begin();

        MultivaluedMap<String, String> headerMap = new MultivaluedHashMap<>();
        headerMap.putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        HttpHeaders headers = new HttpHeaderWrapper(headerMap);

        ExecutionResult result = graphQL.executeQuery(
                payload.getQuery(),
                payload.getOperationName(),
                kcSession, // KeycloakSession
                accessToken, //accessToken
                null, // Request
                headers, // HttpHeaders
                payload.getVariables()
        );


        subscribe(apolloMessage.getId(), result, session);

        kcSession.getTransactionManager().commit();
        kcSession.close();

        return result;
    }

    private void subscribe(String id, ExecutionResult executionResult, Session session) {
        Publisher<ExecutionResult> data = executionResult.getData();

        Disposable subscription = Flux.from(data).subscribe(
                result -> next(result, id, session),
                error -> error(error, id, session),
                () -> complete(id, session)
        );
        synchronized (subscriptions) {
            subscriptions.put(id, subscription);
        }
    }

    private void next(ExecutionResult result, String id, Session session) {
            ApolloMessage m = new ApolloMessage(MessageType.Next, id, result.toSpecification());
            sendMessage(session, m);
    }

    private void error(Throwable error, String id, Session session) {
        GraphQLError e = error instanceof GraphQLError ?
                (GraphQLError) error : GraphQLError.newError().message(error.getMessage()).build();

        ApolloMessage m = new ApolloMessage(MessageType.Error, id, e);
        sendMessage(session, m);
    }

    private void complete(String id, Session session) {
        ApolloMessage m = new ApolloMessage(MessageType.Complete, id);
        sendMessage(session, m);
}

    private void sendMessage(Session session, ApolloMessage message) {
        try {
            sendMessage(session, message.toJson());
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            fatalError(session, e);
        }
    }

    private void fatalError(Session session, Throwable exception) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, exception.getMessage()));
        } catch (Exception suppressed) {
            exception.addSuppressed(suppressed);
        }

        cancelAllSubscriptions();

        LOGGER.error("WebSocket session {} closed due to an exception.", session.getId(), exception);
    }

    void cancelSubscription(String id) {
        synchronized (subscriptions) {
            Disposable d = subscriptions.get(id);
            if (d != null) {
                d.dispose();
                subscriptions.remove(id);
            }
        }
    }
    void cancelAllSubscriptions() {
        synchronized (subscriptions) {
            subscriptions.values().forEach(Disposable::dispose);
            subscriptions.clear();
        }
    }

}
