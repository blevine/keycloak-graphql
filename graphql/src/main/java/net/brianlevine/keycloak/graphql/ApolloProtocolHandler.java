package net.brianlevine.keycloak.graphql;


import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.ExecutionResult;

import graphql.GraphQLError;
import io.netty.handler.codec.http.QueryStringDecoder;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import net.brianlevine.keycloak.graphql.apollo.ApolloMessage;
import net.brianlevine.keycloak.graphql.apollo.MessageType;
import net.brianlevine.keycloak.graphql.apollo.SubscribePayload;
import net.brianlevine.keycloak.graphql.util.Auth;
import net.brianlevine.keycloak.graphql.util.HttpHeaderWrapper;
import org.glassfish.tyrus.core.CloseReasons;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.representations.AccessToken;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static net.brianlevine.keycloak.graphql.Constants.*;
import static net.brianlevine.keycloak.graphql.util.Util.fakeHttpHeadersWithToken;
import static net.brianlevine.keycloak.graphql.util.Util.toIsoDateTime;


public class ApolloProtocolHandler implements WebSocketProtocolHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloProtocolHandler.class);



    private final Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();
    private final GraphQLController graphQL;
    private final static SessionStore sessionStore = SharedSessionStore.getInstance();

    public ApolloProtocolHandler() {
        this.graphQL = new GraphQLController();
    }

    @Override
    public void onOpen(Session session) {
        URI uri = session.getRequestURI();
        Map<String, List<String>> params = new QueryStringDecoder(uri.getQuery()).parameters();
        String realmName = params.containsKey("realm") ? params.get("realm").get(0) : null;
        sessionStore.addSession(session);

        if (realmName == null) {
            LOGGER.warn("Realm name not provided");
        }

        LOGGER.info("Opened: session id = {}, realm = {}", session.getId(), realmName);
    }

    @Override
    public String onMessage(String message, Session session) {
        System.out.println(message);
        return dispatch(session, message);
    }

    @Override
    public void onError(Session session, Throwable error) {
        LOGGER.error("Error: session id = {}", session.getId(), error);

        if (error instanceof NotAuthorizedException) {
            LOGGER.error("Closing connection due to NotAuthorizedException");

            try {
                CloseReason reason = new CloseReason(CloseReasons.VIOLATED_POLICY.getCloseReason().getCloseCode(), "NotAuthorized");
                session.close(reason);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        sessionStore.removeSession(session.getId());
        //if (keepAlive != null) {
        //    keepAlive.cancel(true);
        //}
        LOGGER.info("Closed: session id = {}, reason = {}", session.getId(), closeReason.getReasonPhrase());
    }

    protected String dispatch(Session session, String message) {
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
                        response = doSubscribe(session, apolloMessage);
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
        Object payload = message.getPayload();

        if (payload != null) {
            String realmName = ((Map<String, String>)message.getPayload()).get(REALM_NAME_KEY);
            if (realmName != null) {
                session.getUserProperties().put(REALM_NAME_KEY, realmName);
            }
            else {
                LOGGER.warn("Realm name not provided");
            }

            String accessToken = ((Map<String, String>)message.getPayload()).get(ACCESS_TOKEN_KEY);
            if (accessToken != null) {
                session.getUserProperties().put(ACCESS_TOKEN_KEY, accessToken);

                // If we have a valid access token, set a timer to send a Ping message
                // to the client just before the access token expires. Client should send back a Pong message
                // with a new access token.
                final AccessToken at = verifyAccessToken(session, accessToken, false);

                if (at != null) {
                    Long expiresAt = at.getExp(); // in seconds
                    Date expiresDate = new Date((expiresAt * 1000L) - TOKEN_EXPIRATION_SAFETY);
                    startTokenExpirationTask(session, expiresDate);
                }
                else {
                    throw new NotAuthorizedException("Access token validation failed.");
                }
            }
            else {
                LOGGER.error("No access token found in connection init message payload.");
                throw new NotAuthorizedException("No access token found in connection init message payload.");
            }
        }
        else {
            LOGGER.error("No payload found in connection init message.");
            throw new IllegalStateException("No payload found in connection init message.");
        }

        return response.toJson();
    }

    private AccessToken verifyAccessToken(Session session, String accessToken, boolean throwWhenTokenInvalid) {
        QuarkusKeycloakSessionFactory fac = QuarkusKeycloakSessionFactory.getInstance();
        fac.init();
        KeycloakSession kcSession = fac.create();

        kcSession.getContext().setRealm(getRealm(session, kcSession));
        AccessToken at = Auth.verifyAccessToken(accessToken, kcSession);

        if (at == null && throwWhenTokenInvalid) {
            throw new NotAuthorizedException("Access token validation failed.");
        }

        return at;
    }

    private String doPing(Session session, ApolloMessage message) throws JsonProcessingException{
        LOGGER.info("Got a PING message for session id = {}, with payload {}.", session.getId(), message.getPayload());

        ApolloMessage response = new ApolloMessage(MessageType.Pong, message.getPayload());
        return response.toJson();
    }

    private String doPong(Session session, ApolloMessage message) throws JsonProcessingException{
        LOGGER.info("Got a PONG message for session id = {}, with payload {}.", session.getId(), message.getPayload());

        Map<String, String> payload = (Map<String, String>)message.getPayload();

        if (payload != null) {
            String accessToken = payload.get(ACCESS_TOKEN_KEY);
            if (accessToken != null) {
                LOGGER.info("Received new access token for session id = {}", session.getId());

                verifyAccessToken(session, accessToken, true);

                LOGGER.info("Access token was VALIDATED for session id = {}", session.getId());
                session.getUserProperties().put(ACCESS_TOKEN_KEY, accessToken);
            }
        }

        return null;
    }

    private String doSubscribe(Session session, ApolloMessage message) throws JsonProcessingException {
        ExecutionResult er = execute(session, message);

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

    private ExecutionResult execute(Session session, ApolloMessage apolloMessage) {
        SubscribePayload payload = (SubscribePayload)apolloMessage.getPayload();

        QuarkusKeycloakSessionFactory fac = QuarkusKeycloakSessionFactory.getInstance();
        fac.init();
        KeycloakSession kcSession = fac.create();


        ExecutionResult result;

        try {
            kcSession.getTransactionManager().begin();

            RealmModel realmModel = getRealm(session, kcSession);

            if (realmModel == null) {
                throw new IllegalArgumentException("Could not determine realm. Did you include the 'realm' query parameter in the initial request?");
            }

            kcSession.getContext().setRealm(realmModel);

            String accessToken = null;
            Map<String, Object> props = session.getUserProperties();
            if (props != null) {
                accessToken = (String) props.get(ACCESS_TOKEN_KEY);
            }

            if (accessToken == null) {
                throw new NotAuthorizedException(String.format("No access token found in session (id = %s.", session.getId()));
            }

            // Need to fake HTTP headers because some Keycloak resource classes require the Bearer token in order
            // to do authz.
            HttpHeaders headers = fakeHttpHeadersWithToken(accessToken);

            Map<String, Object> additionalContext = new HashMap<>();
            additionalContext.put(SESSION_ID_KEY, session.getId());

            result = graphQL.executeQuery(
                    payload.getQuery(),
                    payload.getOperationName(),
                    kcSession, // KeycloakSession
                    accessToken, //accessToken
                    null, // Request
                    headers, // HttpHeaders
                    payload.getVariables(),
                    additionalContext
            );


            subscribe(apolloMessage.getId(), result, session, accessToken);

        } finally {
            kcSession.getTransactionManager().commit();
            kcSession.close();
        }



        return result;
    }

    private void subscribe(String id, ExecutionResult executionResult, Session session, String accessToken) {
        Publisher<ExecutionResult> data = executionResult.getData();

        Disposable subscription = Flux.from(data).subscribe(
                result -> next(result, id, session),
                error -> error(error, id, session),
                () -> complete(id, session)
        );
        synchronized (subscriptions) {
            subscriptions.put(id, new SubscriptionInfo(subscription, accessToken));
        }
    }

    private void next(ExecutionResult result, String id, Session session) {
            ApolloMessage m = new ApolloMessage(MessageType.Next, id, result.toSpecification());
            sendMessage(session, m);
    }

    private void error(Throwable error, String id, Session session) {
//        GraphQLError[] e = {error instanceof GraphQLError ?
//                (GraphQLError) error : GraphQLError.newError().message(error.getMessage()).build()};

        GraphQLError.Builder<?> builder = GraphQLError.newError()
                .message(error.getMessage());

        if (error instanceof NotAuthorizedException) {
            Map<String, Object> extensions = new HashMap<>();
            extensions.put("code", "NotAuthorizedException");
            builder.extensions(extensions);
        }

        GraphQLError[] errors = {
                builder.build()
        };


        ApolloMessage m = new ApolloMessage(MessageType.Error, id, errors);

        LOGGER.error("Sending error message for id '{}': {}", m.getId(), m);
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

        cancelSubscription(session.getId());

        LOGGER.error("WebSocket session {} closed due to an exception.", session.getId(), exception);
    }

    void cancelSubscription(String id) {
        synchronized (subscriptions) {
            SubscriptionInfo si = subscriptions.get(id);
            if (si != null) {
                Disposable d = si.subscription;
                if (d != null) {
                    d.dispose();
                    subscriptions.remove(id);
                }
            }
        }
    }
    void cancelAllSubscriptions() {
        synchronized (subscriptions) {
            subscriptions.values().forEach((subscription) -> {
                Disposable d = subscription.subscription;
                if (d != null) {
                    d.dispose();
                }
            });
            subscriptions.clear();
        }
    }

    private RealmModel getRealm(Session session, KeycloakSession kcSession) {
        RealmModel realmModel;

        String realmName = (String)session.getUserProperties().get(REALM_NAME_KEY);

        if (realmName == null) {
            Map<String, List<String>> params = session.getRequestParameterMap();
            realmName = params.containsKey("realm") ? params.get("realm").get(0) : null;
        }

        realmModel = kcSession.realms().getRealmByName(realmName);

        return realmModel;
    }

    /**
     * Set a scheduled task that sends a ping message to the client when the
     * access token is about to expire.
     *
     */
    private void startTokenExpirationTask(Session session, Date date) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Map<String, Object> payload = new HashMap<>();
                payload.put(ACCESS_TOKEN_EXPIRES_KEY, toIsoDateTime(date));
                payload.put(ACCESS_TOKEN_KEY, session.getUserProperties().get(ACCESS_TOKEN_KEY));

                ApolloMessage message = new ApolloMessage(MessageType.Ping, payload);
                sendMessage(session, message);
            }
        }, date);
    }

    private static class SubscriptionInfo {
        public Disposable subscription;
        public String accessToken;

        public SubscriptionInfo(Disposable subscription, String accessToken) {
            this.subscription = subscription;
            this.accessToken = accessToken;
        }
    }

}
