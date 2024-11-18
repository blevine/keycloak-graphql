package net.brianlevine.keycloak.graphql;


import graphql.GraphQL;
import io.vertx.core.AbstractVerticle;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.handler.graphql.ApolloWSHandler;

import net.brianlevine.keycloak.graphql.util.HttpHeaderWrapper;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class SubscriptionServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionServer.class);
    private static final int DEFAULT_PORT = 8081;

    @Override
    public void start() {

        String sPort = System.getenv("SUBSCRIPTION_PORT");
        int port = (sPort != null) ? Integer.parseInt(sPort) : DEFAULT_PORT;

        LOGGER.info("Starting SubscriptionServer on port {}...", port);

        Router router = Router.router(vertx);
        Map<String, Object> context = new HashMap<>();
        context.put("vertex", getVertx());

        GraphQL gql = GraphQLController.getSchema();



        //noinspection deprecation
        ApolloWSHandler h = ApolloWSHandler.create(gql).beforeExecute((a) -> {
            QuarkusKeycloakSessionFactory fac = QuarkusKeycloakSessionFactory.getInstance();
            fac.init();
            KeycloakSession kcSession = fac.create();
            kcSession.getTransactionManager().begin();

            context.put("keycloak.session", kcSession);
            context.put("headers", new HttpHeaderWrapper(a.context().serverWebSocket().headers()));

            a.builder().graphQLContext(context);
        });

        try {
            router.route().path("/graphql").handler(h).failureHandler((ctx -> {
                System.err.println("********Received failure in failure handler");
            }));
        } catch (Exception e) {
            System.err.println("********Received exception in catch block");
        }

        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .addWebSocketSubProtocol("graphql-ws")
                .setLogActivity(true);

        vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .listen(port, "0.0.0.0")
                .onComplete(
                        (e) -> LOGGER.info("SubscriptionServer started on port {}", e.actualPort()),
                        (t) -> LOGGER.error("SubscriptionServer FAILED to start: ", t)
                ).onFailure((e) -> LOGGER.error("SubscriptionServer FAILED to start: ", e));
    }
}
